package org.dromara.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XiangYuPai 用户服务启动类
 * XiangYuPai User Service Application
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@EnableDubbo
@SpringBootApplication
public class XyPaiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyPaiUserApplication.class, args);
        System.out.println("(♥◠‿◠)ノ゙  XiangYuPai User Service started successfully   ლ(´ڡ`ლ)゙");
        System.out.println("  _   _                 ____                  _          ");
        System.out.println(" | | | |___  ___ _ __  / ___|  ___ _ ____   _(_) ___ ___ ");
        System.out.println(" | | | / __|/ _ \\ '__| \\___ \\ / _ \\ '__\\ \\ / / |/ __/ _ \\");
        System.out.println(" | |_| \\__ \\  __/ |     ___) |  __/ |   \\ V /| | (_|  __/");
        System.out.println("  \\___/|___/\\___|_|    |____/ \\___|_|    \\_/ |_|\\___\\___|");
        System.out.println("");
        System.out.println("API Documentation: http://localhost:9401/doc.html");
    }
}
