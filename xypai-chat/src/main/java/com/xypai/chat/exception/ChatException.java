package com.xypai.chat.exception;

import org.dromara.common.core.exception.base.BaseException;

/**
 * 聊天模块异常类(v7.1)
 * 
 * @author xypai
 * @date 2025-01-14
 */
public class ChatException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ChatException(String code, Object[] args) {
        super("chat", code, args, null);
    }

    public ChatException(String code, Object[] args, String defaultMessage) {
        super("chat", code, args, defaultMessage);
    }

    /**
     * 消息发送失败
     */
    public static ChatException messageSendFailed(String reason) {
        return new ChatException("message.send.failed", 
                new Object[]{reason}, "消息发送失败：" + reason);
    }

    /**
     * 消息撤回失败
     */
    public static ChatException recallFailed(String reason) {
        return new ChatException("message.recall.failed", 
                new Object[]{reason}, "消息撤回失败：" + reason);
    }

    /**
     * 会话不存在
     */
    public static ChatException conversationNotFound(Long conversationId) {
        return new ChatException("conversation.not.found", 
                new Object[]{conversationId}, "会话不存在：" + conversationId);
    }

    /**
     * 无权限访问会话
     */
    public static ChatException conversationAccessDenied(Long conversationId) {
        return new ChatException("conversation.access.denied", 
                new Object[]{conversationId}, "无权限访问会话：" + conversationId);
    }

    /**
     * 消息去重失败（重复消息）
     */
    public static ChatException duplicateMessage(String clientId) {
        return new ChatException("message.duplicate", 
                new Object[]{clientId}, "消息重复：" + clientId);
    }

    /**
     * 超出文件大小限制
     */
    public static ChatException fileSizeExceeded(Long actualSize, Long maxSize) {
        return new ChatException("file.size.exceeded", 
                new Object[]{actualSize, maxSize}, 
                String.format("文件大小超出限制：%d > %d", actualSize, maxSize));
    }

    /**
     * 超出时长限制
     */
    public static ChatException durationExceeded(Integer actualDuration, Integer maxDuration) {
        return new ChatException("duration.exceeded", 
                new Object[]{actualDuration, maxDuration}, 
                String.format("时长超出限制：%d > %d", actualDuration, maxDuration));
    }

    /**
     * 超出成员数量限制
     */
    public static ChatException memberLimitExceeded(Integer currentCount, Integer maxCount) {
        return new ChatException("member.limit.exceeded", 
                new Object[]{currentCount, maxCount}, 
                String.format("成员数量超出限制：%d > %d", currentCount, maxCount));
    }

    /**
     * WebSocket连接失败
     */
    public static ChatException websocketConnectionFailed(String reason) {
        return new ChatException("websocket.connection.failed", 
                new Object[]{reason}, "WebSocket连接失败：" + reason);
    }

    /**
     * Token验证失败
     */
    public static ChatException tokenValidationFailed() {
        return new ChatException("token.validation.failed", 
                new Object[]{}, "Token验证失败");
    }
}
