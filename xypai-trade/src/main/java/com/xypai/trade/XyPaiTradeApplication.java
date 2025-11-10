package com.xypai.trade;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 交易模块
 *
 * @author xypai
 */
@EnableDubbo
@SpringBootApplication
public class XyPaiTradeApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiTradeApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  交易模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
