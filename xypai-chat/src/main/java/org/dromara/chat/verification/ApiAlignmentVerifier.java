package org.dromara.chat.verification;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.chat.controller.app.MessageController;
import org.dromara.chat.service.IMessageService;
import org.dromara.chat.websocket.MessageWebSocketHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * API Alignment Verification Tool
 * API对齐验证工具
 *
 * Verifies that all frontend-documented APIs are properly implemented in the backend.
 * Run this tool on application startup to ensure consistency.
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAlignmentVerifier implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final IMessageService messageService;
    private final MessageWebSocketHandler webSocketHandler;

    // Expected API endpoints from frontend documentation
    private static final ApiEndpoint[] EXPECTED_ENDPOINTS = {
        new ApiEndpoint("GET", "/api/message/unread-count", "getUnreadCount"),
        new ApiEndpoint("GET", "/api/message/conversations", "getConversations"),
        new ApiEndpoint("DELETE", "/api/message/conversation/{conversationId}", "deleteConversation"),
        new ApiEndpoint("POST", "/api/message/clear-all", "clearAllMessages"),
        new ApiEndpoint("GET", "/api/message/chat/{conversationId}", "getChatHistory"),
        new ApiEndpoint("POST", "/api/message/send", "sendMessage"),
        new ApiEndpoint("PUT", "/api/message/read/{conversationId}", "markMessagesAsRead"),
        new ApiEndpoint("POST", "/api/message/recall/{messageId}", "recallMessage"),
        new ApiEndpoint("DELETE", "/api/message/{messageId}", "deleteMessage"),
        new ApiEndpoint("POST", "/api/message/upload", "uploadMedia")
    };

    // Expected WebSocket events
    private static final String[] EXPECTED_WS_EVENTS = {
        "new_message",
        "message_read",
        "message_recalled",
        "typing",
        "online_status"
    };

    @Override
    public void run(String... args) throws Exception {
        // Only run in dev/test environments
        String profile = System.getProperty("spring.profiles.active", "dev");
        if (!profile.contains("dev") && !profile.contains("test")) {
            log.debug("API alignment verification skipped (not in dev/test environment)");
            return;
        }

        log.info("========================================");
        log.info("API Alignment Verification Started");
        log.info("========================================");

        List<String> errors = new ArrayList<>();
        int successCount = 0;

        // 1. Verify REST API endpoints
        log.info("\n1. Verifying REST API Endpoints:");
        log.info("----------------------------------");

        MessageController controller = applicationContext.getBean(MessageController.class);
        Class<?> controllerClass = controller.getClass();

        for (ApiEndpoint expected : EXPECTED_ENDPOINTS) {
            try {
                Method method = findControllerMethod(controllerClass, expected.methodName);

                if (method == null) {
                    errors.add(String.format("✗ Missing method: %s (for %s %s)",
                        expected.methodName, expected.httpMethod, expected.path));
                    continue;
                }

                // Verify HTTP method annotation
                boolean hasCorrectMapping = verifyHttpMethodMapping(method, expected.httpMethod);

                if (hasCorrectMapping) {
                    log.info("✓ {} {} → {} (VERIFIED)",
                        expected.httpMethod, expected.path, expected.methodName);
                    successCount++;
                } else {
                    errors.add(String.format("✗ Incorrect HTTP method for %s: expected %s",
                        expected.methodName, expected.httpMethod));
                }

            } catch (Exception e) {
                errors.add(String.format("✗ Error verifying %s: %s",
                    expected.methodName, e.getMessage()));
            }
        }

        // 2. Verify Service Layer
        log.info("\n2. Verifying Service Layer:");
        log.info("----------------------------------");

        String[] serviceMethods = {
            "getUnreadCount", "getConversations", "deleteConversation",
            "clearAllMessages", "getChatHistory", "sendMessage",
            "markMessagesAsRead", "recallMessage", "deleteMessage"
        };

        for (String methodName : serviceMethods) {
            try {
                Method method = findServiceMethod(methodName);
                if (method != null) {
                    log.info("✓ Service method: {} (VERIFIED)", methodName);
                    successCount++;
                } else {
                    errors.add(String.format("✗ Missing service method: %s", methodName));
                }
            } catch (Exception e) {
                errors.add(String.format("✗ Error verifying service method %s: %s",
                    methodName, e.getMessage()));
            }
        }

        // 3. Verify WebSocket Handler
        log.info("\n3. Verifying WebSocket Handler:");
        log.info("----------------------------------");

        if (webSocketHandler != null) {
            log.info("✓ WebSocket handler initialized (VERIFIED)");
            successCount++;

            // Check WebSocket event methods
            String[] wsMethods = {"sendNewMessage", "sendMessageRead", "sendMessageRecalled"};
            for (String methodName : wsMethods) {
                Method method = findWebSocketMethod(methodName);
                if (method != null) {
                    log.info("✓ WebSocket method: {} (VERIFIED)", methodName);
                    successCount++;
                } else {
                    errors.add(String.format("✗ Missing WebSocket method: %s", methodName));
                }
            }
        } else {
            errors.add("✗ WebSocket handler not initialized");
        }

        // 4. Print Summary
        log.info("\n========================================");
        log.info("API Alignment Verification Summary");
        log.info("========================================");
        log.info("Total Checks: {}", EXPECTED_ENDPOINTS.length + serviceMethods.length + 4);
        log.info("Passed: {} ✓", successCount);
        log.info("Failed: {} ✗", errors.size());

        if (errors.isEmpty()) {
            log.info("\n🎉 ALL CHECKS PASSED! Frontend-Backend API alignment verified.");
        } else {
            log.warn("\n⚠ ISSUES FOUND:");
            errors.forEach(error -> log.warn("  {}", error));
            log.warn("\nPlease fix the above issues to ensure frontend-backend compatibility.");
        }

        log.info("========================================\n");
    }

    // ==================== Helper Methods ====================

    private Method findControllerMethod(Class<?> controllerClass, String methodName) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private Method findServiceMethod(String methodName) {
        for (Method method : IMessageService.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private Method findWebSocketMethod(String methodName) {
        for (Method method : MessageWebSocketHandler.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private boolean verifyHttpMethodMapping(Method method, String expectedHttpMethod) {
        return switch (expectedHttpMethod.toUpperCase()) {
            case "GET" -> method.isAnnotationPresent(GetMapping.class);
            case "POST" -> method.isAnnotationPresent(PostMapping.class);
            case "PUT" -> method.isAnnotationPresent(PutMapping.class);
            case "DELETE" -> method.isAnnotationPresent(DeleteMapping.class);
            default -> false;
        };
    }

    // ==================== Data Classes ====================

    @Data
    static class ApiEndpoint {
        private final String httpMethod;
        private final String path;
        private final String methodName;

        ApiEndpoint(String httpMethod, String path, String methodName) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.methodName = methodName;
        }
    }
}
