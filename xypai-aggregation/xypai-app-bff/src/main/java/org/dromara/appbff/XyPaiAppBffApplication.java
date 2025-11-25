package org.dromara.appbff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.ComponentScan;

/**
 * XyPai App BFF 聚合服务启动类
 *
 * @author XyPai Team
 * @date 2025-11-20
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.dromara.appbff", "org.dromara.common"})
public class XyPaiAppBffApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiAppBffApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ノ゙  XyPai App BFF聚合服务启动成功   ლ(´ڡ`ლ)゙");
    }

}
