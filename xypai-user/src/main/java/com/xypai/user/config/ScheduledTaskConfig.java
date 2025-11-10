package com.xypai.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置
 *
 * @author Bob
 * @date 2025-01-14
 */
@Configuration
@EnableScheduling
public class ScheduledTaskConfig {
    
    // Spring Boot会自动启用定时任务
    // 任务实现在 task 包中
}

