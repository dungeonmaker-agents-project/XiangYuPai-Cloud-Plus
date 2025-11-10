package com.xypai.chat.controller.app;

import com.xypai.chat.websocket.ChatWebSocketServer;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket管理控制�?(v7.1新增)
 * 
 * 提供WebSocket连接状态查询、在线用户统计等功能
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Tag(name = "WebSocket管理", description = "WebSocket连接管理API (v7.1)")
@RestController
@RequestMapping("/api/v1/websocket")
@RequiredArgsConstructor
public class WebSocketManagementController extends BaseController {

    /**
     * 获取在线人数
     */
    @Operation(summary = "获取在线人数", description = "获取当前WebSocket在线连接数")
    @GetMapping("/online-count")
    @SaCheckPermission("chat:websocket:query")
    public R<Map<String, Object>> getOnlineCount() {
        int onlineCount = ChatWebSocketServer.getOnlineCount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("onlineCount", onlineCount);
        result.put("timestamp", System.currentTimeMillis());
        
        return R.ok(result);
    }

    /**
     * 检查用户是否在线
     */
    @Operation(summary = "检查用户是否在线", description = "检查指定用户是否在线")
    @GetMapping("/is-online/{userId}")
    @SaCheckPermission("chat:websocket:query")
    public R<Map<String, Object>> isUserOnline(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        
        boolean isOnline = ChatWebSocketServer.isUserOnline(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("isOnline", isOnline);
        result.put("timestamp", System.currentTimeMillis());
        
        return R.ok(result);
    }

    /**
     * 获取所有在线用户ID
     */
    @Operation(summary = "获取所有在线用户", description = "获取所有在线用户ID列表")
    @GetMapping("/online-users")
    @SaCheckPermission("chat:websocket:query")
    public R<Map<String, Object>> getOnlineUsers() {
        List<Long> onlineUserIds = ChatWebSocketServer.getOnlineUserIds();
        
        Map<String, Object> result = new HashMap<>();
        result.put("userIds", onlineUserIds);
        result.put("count", onlineUserIds.size());
        result.put("timestamp", System.currentTimeMillis());
        
        return R.ok(result);
    }

    /**
     * 强制断开用户连接（管理员功能�?
     */
    @Operation(summary = "强制断开连接", description = "管理员强制断开指定用户的WebSocket连接")
    @DeleteMapping("/disconnect/{userId}")
    @SaCheckPermission("chat:websocket:admin")
    public R<Void> disconnectUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "断开原因")
            @RequestParam(required = false, defaultValue = "管理员操作") String reason) {
        
        boolean success = ChatWebSocketServer.disconnectUser(userId, reason);
        
        return success ? R.ok() : R.fail("用户不在线或断开失败");
    }

    /**
     * 系统广播消息（管理员功能）
     */
    @Operation(summary = "系统广播", description = "管理员向所有在线用户广播消息")
    @PostMapping("/broadcast")
    @SaCheckPermission("chat:websocket:admin")
    public R<Map<String, Object>> broadcast(
            @Parameter(description = "广播内容", required = true)
            @RequestParam String content) {
        
        List<Long> onlineUserIds = ChatWebSocketServer.getOnlineUserIds();
        int successCount = 0;
        
        for (Long userId : onlineUserIds) {
            try {
                Map<String, Object> message = new HashMap<>();
                message.put("type", "system");
                message.put("content", content);
                message.put("timestamp", System.currentTimeMillis());
                
                ChatWebSocketServer.sendMessageToUser(userId, message);
                successCount++;
            } catch (Exception e) {
                // 继续广播给其他用户
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", onlineUserIds.size());
        result.put("successCount", successCount);
        result.put("content", content);
        
        return R.ok(result);
    }
}

