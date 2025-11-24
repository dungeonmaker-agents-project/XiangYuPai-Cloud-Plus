package org.dromara.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XiangYuPai Payment Service - 支付服务
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@SpringBootApplication
public class XyPaiPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyPaiPaymentApplication.class, args);
        System.out.println("""

                ========================================
                   XiangYuPai Payment Service Started
                   支付服务启动成功
                   Port: 9411
                ========================================
                """);
    }
}
