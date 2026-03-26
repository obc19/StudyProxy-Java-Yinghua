package com.studyproxy;

import com.studyproxy.action.YinghuaCourseAction;
import com.studyproxy.action.YinghuaLoginAction;
import com.studyproxy.entity.AccountConfig;
import com.studyproxy.entity.yinghua.YinghuaCourse;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import com.studyproxy.service.yinghua.YinghuaVideoStudyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
public class StudyProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyProxyApplication.class, args);
    }

    @Bean
    public CommandLineRunner consoleRunner(
            AccountConfig accountConfig,
            YinghuaLoginAction yinghuaLoginAction,
            YinghuaCourseAction yinghuaCourseAction,
            YinghuaVideoStudyService yinghuaVideoStudyService) {
        
        return args -> {
            printBanner();
            
            List<AccountConfig.YinghuaAccount> accounts = accountConfig.getYinghua();
            
            if (accounts == null || accounts.isEmpty()) {
                log.error("未配置英华学堂账号，请检查config.yaml");
                System.exit(1);
            }
            
            log.info("共配置 {} 个账号，开始并行刷课...", accounts.size());
            
            int accountThreads = Math.min(accounts.size(), 100);
            ExecutorService accountExecutor = Executors.newFixedThreadPool(accountThreads);
            CountDownLatch latch = new CountDownLatch(accounts.size());
            
            for (int i = 0; i < accounts.size(); i++) {
                final int index = i + 1;
                AccountConfig.YinghuaAccount account = accounts.get(i);
                
                accountExecutor.submit(() -> {
                    try {
                        processAccount(index, accounts.size(), account, 
                                yinghuaLoginAction, yinghuaCourseAction, yinghuaVideoStudyService);
                    } catch (Exception e) {
                        log.error("[账号{}] {} 处理失败: {}", index, account.getAccount(), e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
                accountExecutor.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("刷课被中断");
            }
            
            log.info("========== 所有账号刷课完成 ==========");
            System.exit(0);
        };
    }

    private void printBanner() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("       StudyProxy-Java v1.0.0           ");
        System.out.println("       英华学堂自动学习工具              ");
        System.out.println("========================================");
        System.out.println();
    }

    private void processAccount(int index, int total, 
                               AccountConfig.YinghuaAccount account,
                               YinghuaLoginAction yinghuaLoginAction,
                               YinghuaCourseAction yinghuaCourseAction,
                               YinghuaVideoStudyService yinghuaVideoStudyService) {
        String accountName = account.getAccount();
        
        log.info("[账号{}/{}] {} 开始处理", index, total, accountName);
        
        try {
            YinghuaUserCache userCache = new YinghuaUserCache();
            userCache.setAccount(account.getAccount());
            userCache.setPassword(account.getPassword());
            userCache.setPreUrl(account.getPreUrl());
            
            log.info("[{}] 正在登录...", accountName);
            yinghuaLoginAction.login(userCache);
            log.info("[{}] 登录成功", accountName);
            
            log.info("[{}] 正在获取课程列表...", accountName);
            List<YinghuaCourse> courses = yinghuaCourseAction.pullCourseList(userCache);
            log.info("[{}] 获取到 {} 门课程", accountName, courses.size());
            
            if (!courses.isEmpty()) {
                log.info("[{}] 开始并行刷课...", accountName);
                parallelStudy(userCache, courses, yinghuaVideoStudyService);
                log.info("[{}] 所有课程刷课完成", accountName);
            }
            
        } catch (Exception e) {
            log.error("[{}] 处理失败: {}", accountName, e.getMessage(), e);
        }
    }

    private void parallelStudy(YinghuaUserCache userCache, 
                              List<YinghuaCourse> courses,
                              YinghuaVideoStudyService yinghuaVideoStudyService) {
        int threadCount = Math.min(courses.size(), 5);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(courses.size());
        
        for (YinghuaCourse course : courses) {
            executor.submit(() -> {
                try {
                    log.info("[{}] 开始刷课: {} (进度: {}%)", 
                            userCache.getAccount(), course.getName(), String.format("%.1f", course.getProgress()));
                    yinghuaVideoStudyService.studyCourse(userCache, course);
                    log.info("[{}] 课程完成: {}", userCache.getAccount(), course.getName());
                } catch (Exception e) {
                    log.error("[{}] 课程 {} 刷课失败: {}", 
                            userCache.getAccount(), course.getName(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
