package com.ruoyi.system.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AiExecutorConfig {
    @Bean("aiCmdExecExecutor")
    public Executor aiCmdExecExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(10);
        exec.setMaxPoolSize(20);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("ai-cmd-exec-");
        exec.initialize();
        return exec;
    }
}
