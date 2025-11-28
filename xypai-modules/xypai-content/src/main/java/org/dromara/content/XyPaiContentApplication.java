package org.dromara.content;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 内容服务
 *
 * @author XiangYuPai
 */
@EnableDubbo
@SpringBootApplication
public class XyPaiContentApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiContentApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ノ゙  XiangYuPai Content Service Started Successfully   ლ(´ڡ`ლ)゙  ");
    }

}
