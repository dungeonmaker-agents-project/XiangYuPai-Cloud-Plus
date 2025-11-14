package com.xypai.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 用户模块（向娱拍版）
 * 
 * 已启用数据库连接：xypai_user
 *
 * @author xypai
 */
@EnableDubbo
@SpringBootApplication
@MapperScan("com.xypai.user.mapper")
public class  XyPaiUserApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiUserApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  用户模块启动成功（向娱拍版 - 已连接xypai_user数据库）  ლ(´ڡ`ლ)");
    }
}
