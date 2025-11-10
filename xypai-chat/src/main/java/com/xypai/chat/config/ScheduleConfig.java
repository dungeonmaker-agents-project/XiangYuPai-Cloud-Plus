package com.xypai.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类 (v7.1)
 * 
 * 启用Spring Task定时任务
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {
    // Spring Task自动扫描@Scheduled注解
}

