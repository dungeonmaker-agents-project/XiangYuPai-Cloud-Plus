package com.xypai.chat.controller.app;

import com.xypai.chat.websocket.ChatWebSocketServer;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 正在输入状态控制器 (v7.1新增)
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Slf4j
@Tag(name = "正在输入状态", description = "正在输入状态管理API (v7.1)")
@RestController
@RequestMapping("/api/v1/typing")
@RequiredArgsConstructor
public class TypingStatusController extends BaseController {

    /**
     * 更新正在输入状态
     */
    @Operation(summary = "更新正在输入状态", description = "通知其他用户正在输入")
    @PostMapping
    @SaCheckPermission("chat:message:add")
    public R<Void> updateTypingStatus(
            @Parameter(description = "会话ID", required = true)
            @RequestParam Long conversationId,
            @Parameter(description = "是否正在输入", required = true)
            @RequestParam Boolean isTyping) {
        
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return R.fail("未获取到当前用户信息");
        }

        // TODO: 使用Redis存储输入状态
        // String key = "typing:" + conversationId + ":" + userId;
        // if (isTyping) {
        //     redisTemplate.opsForValue().set(key, "1", 10, TimeUnit.SECONDS);
        // } else {
        //     redisTemplate.delete(key);
        // }

        // v7.1: 通过WebSocket推送给其他成员
        // ChatWebSocketServer.broadcastToConversation(conversationId, typingMessage, userId);

        log.debug("更新正在输入状态，会话ID：{}，用户ID：{}，是否输入：{}", 
                conversationId, userId, isTyping);
        
        return R.ok();
    }

    /**
     * 获取会话中正在输入的用户
     */
    @Operation(summary = "获取正在输入的用户", description = "获取会话中正在输入的用户列表")
    @GetMapping("/{conversationId}")
    @SaCheckPermission("chat:message:query")
    public R<Map<String, Object>> getTypingUsers(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        
        // TODO: 从Redis查询正在输入的用户
        // String pattern = "typing:" + conversationId + ":*";
        // Set<String> keys = redisTemplate.keys(pattern);
        // List<Long> typingUserIds = keys.stream()
        //     .map(key -> key.split(":")[2])
        //     .map(Long::parseLong)
        //     .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversationId);
        result.put("typingUsers", new java.util.ArrayList<>());
        
        return R.ok(result);
    }
}
