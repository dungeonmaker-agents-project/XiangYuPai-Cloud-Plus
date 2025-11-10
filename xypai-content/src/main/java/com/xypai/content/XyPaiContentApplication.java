package com.xypai.content;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 内容模块
 * 
 * v7.1升级�?
 * - 添加空间索引查询（地理位置）
 * - 添加Redis统计缓存（性能提升50倍）
 * - 添加评论系统（多级评论）
 * - 添加草稿自动保存
 *
 * @author xypai
 */
@EnableDubbo
@EnableScheduling  // v7.1新增：启用定时任务（统计同步、草稿清理）
@SpringBootApplication
public class XyPaiContentApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiContentApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  内容模块启动成功 v7.1   ╰(*´︶`*)╯");
    }
}
