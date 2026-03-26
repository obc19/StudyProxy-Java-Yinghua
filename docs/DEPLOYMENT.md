# 部署文档

## 1. 环境要求

### 1.1 硬件要求

| 配置项 | 最低要求 | 推荐配置 |
|--------|----------|----------|
| CPU | 2核 | 4核+ |
| 内存 | 2GB | 4GB+ |
| 磁盘 | 1GB | 10GB+ |
| 网络 | 1Mbps | 10Mbps+ |

### 1.2 软件要求

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 1.8+ | Java运行环境 |
| Python | 3.8+ | OCR服务 |
| pip | 20.0+ | Python包管理 |

## 2. 本地部署

### 2.1 安装JDK

#### Windows

1. 下载JDK: https://www.oracle.com/java/technologies/downloads/
2. 安装并配置环境变量：
   - `JAVA_HOME`: JDK安装目录
   - `Path`: 添加 `%JAVA_HOME%\bin`

3. 验证安装：
```bash
java -version
```

#### Linux

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-8-jdk

# CentOS/RHEL
sudo yum install java-1.8.0-openjdk

# 验证
java -version
```

### 2.2 安装Python

#### Windows

1. 下载Python: https://www.python.org/downloads/
2. 安装时勾选 "Add Python to PATH"
3. 验证安装：
```bash
python --version
pip --version
```

#### Linux

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install python3 python3-pip

# CentOS/RHEL
sudo yum install python3 python3-pip

# 验证
python3 --version
pip3 --version
```

### 2.3 安装OCR服务

```bash
pip install ddddocr flask
```

### 2.4 运行项目

#### 方式一：Maven运行

```bash
# 克隆项目
git clone https://github.com/xxx/StudyProxy-Java-yinghua.git
cd StudyProxy-Java-yinghua

# 安装Maven (如果没有)
# Windows: 下载 https://maven.apache.org/download.cgi
# Linux: sudo apt install maven

# 编译运行
mvn spring-boot:run
```

#### 方式二：JAR包运行

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/StudyProxy-Java-1.0.0.jar
```

## 3. 服务器部署

### 3.1 准备工作

```bash
# 创建目录
sudo mkdir -p /opt/study-proxy
cd /opt/study-proxy

# 上传文件
# - StudyProxy-Java-1.0.0.jar
# - config.yaml
# - ocr_service.py
```

### 3.2 启动OCR服务

#### 创建OCR服务脚本

```bash
sudo nano /etc/systemd/system/ocr-service.service
```

内容：

```ini
[Unit]
Description=OCR Service for StudyProxy
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/study-proxy
ExecStart=/usr/bin/python3 /opt/study-proxy/ocr_service.py
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable ocr-service
sudo systemctl start ocr-service
sudo systemctl status ocr-service
```

### 3.3 启动主程序

#### 方式一：Systemd服务

创建服务文件：

```bash
sudo nano /etc/systemd/system/study-proxy.service
```

内容：

```ini
[Unit]
Description=StudyProxy Java Application
After=network.target ocr-service.service
Requires=ocr-service.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/study-proxy
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/study-proxy/StudyProxy-Java-1.0.0.jar
Restart=always
RestartSec=30
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable study-proxy
sudo systemctl start study-proxy
sudo systemctl status study-proxy
```

查看日志：

```bash
journalctl -u study-proxy -f
```

#### 方式二：Screen后台运行

```bash
# 安装screen
sudo apt install screen

# 创建会话
screen -S study-proxy

# 运行程序
cd /opt/study-proxy
java -jar StudyProxy-Java-1.0.0.jar

# 按 Ctrl+A+D 退出会话
# 重新连接: screen -r study-proxy
```

#### 方式三：NoHup后台运行

```bash
nohup java -jar StudyProxy-Java-1.0.0.jar > output.log 2>&1 &

# 查看日志
tail -f output.log

# 查看进程
ps aux | grep java

# 停止进程
kill <pid>
```

## 4. Docker部署

### 4.1 构建镜像

创建 `Dockerfile`：

```dockerfile
FROM openjdk:8-jre-slim

# 安装Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install ddddocr flask && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制文件
COPY target/StudyProxy-Java-1.0.0.jar app.jar
COPY config.yaml config.yaml
COPY ocr_service.py ocr_service.py

# 暴露端口 (OCR服务)
EXPOSE 5000

# 启动脚本
RUN echo '#!/bin/bash\npython3 /app/ocr_service.py &\njava -jar /app/app.jar' > start.sh && \
    chmod +x start.sh

CMD ["./start.sh"]
```

构建镜像：

```bash
mvn clean package -DskipTests
docker build -t study-proxy:latest .
```

### 4.2 运行容器

```bash
docker run -d \
  --name study-proxy \
  --restart always \
  -v $(pwd)/config.yaml:/app/config.yaml \
  -v $(pwd)/logs:/app/logs \
  study-proxy:latest
```

### 4.3 Docker Compose

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  ocr-service:
    image: python:3.9-slim
    container_name: ocr-service
    restart: always
    working_dir: /app
    volumes:
      - ./ocr_service.py:/app/ocr_service.py
    command: >
      bash -c "pip install ddddocr flask && python ocr_service.py"
    ports:
      - "5000:5000"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:5000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  study-proxy:
    image: openjdk:8-jre-slim
    container_name: study-proxy
    restart: always
    depends_on:
      ocr-service:
        condition: service_healthy
    working_dir: /app
    volumes:
      - ./target/StudyProxy-Java-1.0.0.jar:/app/app.jar
      - ./config.yaml:/app/config.yaml
      - ./logs:/app/logs
    environment:
      - OCR_SERVICE_URL=http://ocr-service:5000
    command: java -jar app.jar
```

运行：

```bash
docker-compose up -d
```

## 5. Nginx反向代理

### 5.1 配置示例

```nginx
upstream ocr_backend {
    server 127.0.0.1:5000;
}

server {
    listen 80;
    server_name ocr.example.com;

    location / {
        proxy_pass http://ocr_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 6. 监控与日志

### 6.1 日志配置

日志文件位置：`logs/study-proxy.log`

日志轮转配置：

```xml
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>logs/study-proxy.%d{yyyy-MM-dd}.log</fileNamePattern>
    <maxHistory>30</maxHistory>
    <totalSizeCap>1GB</totalSizeCap>
</rollingPolicy>
```

### 6.2 日志查看

```bash
# 实时查看
tail -f logs/study-proxy.log

# 查看最近100行
tail -n 100 logs/study-proxy.log

# 搜索错误
grep "ERROR" logs/study-proxy.log

# 按日期查看
grep "2024-01-15" logs/study-proxy.log
```

### 6.3 进程监控

```bash
# 查看Java进程
ps aux | grep java

# 查看内存使用
top -p $(pgrep -f StudyProxy)

# 查看端口占用
netstat -tlnp | grep java
```

## 7. 性能优化

### 7.1 JVM参数

```bash
java -Xms512m \
     -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/opt/study-proxy/dumps \
     -jar StudyProxy-Java-1.0.0.jar
```

### 7.2 并发优化

根据服务器配置调整并发数：

| 服务器配置 | 账号并发 | 课程并发 |
|-----------|---------|---------|
| 2核2G | 10 | 3 |
| 4核4G | 50 | 5 |
| 8核8G | 100 | 5 |

### 7.3 网络优化

```yaml
studyproxy:
  http:
    connect-timeout: 30000
    read-timeout: 30000
    write-timeout: 30000
    retry-times: 3
```

## 8. 备份与恢复

### 8.1 配置备份

```bash
# 备份配置
cp config.yaml config.yaml.bak

# 定时备份
crontab -e
# 添加: 0 0 * * * cp /opt/study-proxy/config.yaml /opt/backup/config.yaml.$(date +\%Y\%m\%d)
```

### 8.2 日志备份

```bash
# 打包日志
tar -czf logs-$(date +%Y%m%d).tar.gz logs/

# 删除30天前的日志
find logs/ -name "*.log" -mtime +30 -delete
```

## 9. 故障排查

### 9.1 常见问题

#### OCR服务无法启动

```bash
# 检查端口占用
netstat -tlnp | grep 5000

# 检查Python环境
python3 -c "import ddddocr; import flask; print('OK')"

# 手动启动测试
python3 ocr_service.py
```

#### Java程序无法启动

```bash
# 检查JDK版本
java -version

# 检查配置文件
cat config.yaml

# 前台运行查看错误
java -jar StudyProxy-Java-1.0.0.jar
```

#### 内存不足

```bash
# 查看内存使用
free -m

# 调整JVM参数
java -Xmx1g -jar StudyProxy-Java-1.0.0.jar
```

### 9.2 日志分析

```bash
# 统计错误数量
grep -c "ERROR" logs/study-proxy.log

# 查看最近的错误
grep "ERROR" logs/study-proxy.log | tail -20

# 分析登录失败
grep "登录失败" logs/study-proxy.log
```

## 10. 安全加固

### 10.1 文件权限

```bash
# 设置配置文件权限
chmod 600 config.yaml

# 设置日志目录权限
chmod 750 logs/
```

### 10.2 防火墙配置

```bash
# 仅允许本地访问OCR服务
sudo ufw allow from 127.0.0.1 to any port 5000

# 查看防火墙状态
sudo ufw status
```

### 10.3 敏感信息保护

使用环境变量：

```bash
export DB_PASSWORD="your_password"
java -jar StudyProxy-Java-1.0.0.jar
```

## 11. 更新升级

### 11.1 更新步骤

```bash
# 1. 停止服务
sudo systemctl stop study-proxy

# 2. 备份
cp StudyProxy-Java-1.0.0.jar StudyProxy-Java-1.0.0.jar.bak

# 3. 更新JAR包
# 上传新的JAR包

# 4. 启动服务
sudo systemctl start study-proxy

# 5. 检查状态
sudo systemctl status study-proxy
```

### 11.2 版本回滚

```bash
# 停止服务
sudo systemctl stop study-proxy

# 恢复旧版本
cp StudyProxy-Java-1.0.0.jar.bak StudyProxy-Java-1.0.0.jar

# 启动服务
sudo systemctl start study-proxy
```
