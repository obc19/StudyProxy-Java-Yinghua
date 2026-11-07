# 用阿里云镜像地址
FROM registry.cn-hangzhou.aliyuncs.com/library/python:3.11-alpine
WORKDIR /app
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && \
    apk add --no-cache libgl libglib2.0 curl && rm -rf /var/cache/apk/*
COPY ocr_service.py .
RUN pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/
RUN pip install --no-cache-dir flask==3.0.0 ddddocr==1.6.1 requests==2.31.0
EXPOSE 8083
ENTRYPOINT ["python", "ocr_service.py"]