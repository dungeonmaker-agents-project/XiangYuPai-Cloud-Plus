package org.dromara.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * XiangYuPai Chat & Messaging Service
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@SpringBootApplication
public class XyPaiChatApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XyPaiChatApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("""

            ============================================
            ✅ XiangYuPai Chat Service Started Successfully!
            📡 Port: 9404
            📖 API Docs: http://localhost:9404/doc.html
            🔌 WebSocket: ws://localhost:9404/ws
            ============================================
            """);
    }
}
