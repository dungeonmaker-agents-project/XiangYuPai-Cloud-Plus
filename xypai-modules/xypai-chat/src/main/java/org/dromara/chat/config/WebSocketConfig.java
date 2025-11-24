package org.dromara.chat.config;

import lombok.RequiredArgsConstructor;
import org.dromara.chat.websocket.MessageWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration
 * WebSocket配置类
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws")
            .setAllowedOrigins("*")  // In production, configure specific origins
            .withSockJS();  // Enable SockJS fallback for browsers that don't support WebSocket
    }
}
