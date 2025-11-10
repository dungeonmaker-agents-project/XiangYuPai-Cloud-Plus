package com.xypai.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 用户模块
 *
 * @author xypai
 */
@EnableDubbo
@SpringBootApplication
public class XyPaiUserApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiUserApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  用户模块启动成功   ლ(´ڡ`ლ)");
    }
}
