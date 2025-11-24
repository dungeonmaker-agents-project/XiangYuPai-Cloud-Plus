package org.dromara.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XiangYuPai Order Service - 订单服务
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@SpringBootApplication
public class XyPaiOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyPaiOrderApplication.class, args);
        System.out.println("""

                ========================================
                   XiangYuPai Order Service Started
                   订单服务启动成功
                   Port: 9410
                ========================================
                """);
    }
}
