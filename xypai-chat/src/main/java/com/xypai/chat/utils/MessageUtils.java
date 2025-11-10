package com.xypai.chat.utils;

import com.xypai.chat.domain.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

/**
 * 消息工具类 (v7.1)
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Slf4j
public class MessageUtils {

    /**
     * 生成客户端消息ID（UUID）
     * 
     * @return UUID字符串
     */
    public static String generateClientId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成消息序列号（临时方案）
     * 
     * v7.1 TODO: 使用Redis INCR实现全局递增
     * 
     * @param conversationId 会话ID
     * @return 序列号
     */
    public static Long generateSequenceId(Long conversationId) {
        // 临时方案：使用时间戳 + 会话ID哈希
        long timestamp = System.currentTimeMillis();
        int hash = conversationId != null ? conversationId.hashCode() % 1000 : 0;
        return timestamp * 1000 + Math.abs(hash);
    }

    /**
     * 生成消息预览文本
     * 
     * @param messageType 消息类型
     * @param content 消息内容
     * @param mediaData 媒体数据
     * @param maxLength 最大长度
     * @return 预览文本
     */
    public static String generatePreview(Integer messageType, String content, 
                                        Map<String, Object> mediaData, int maxLength) {
        if (messageType == null) {
            return truncateText(content, maxLength);
        }

        ChatMessage.MessageType type = ChatMessage.MessageType.fromCode(messageType);
        if (type == null) {
            return truncateText(content, maxLength);
        }

        switch (type) {
            case TEXT:
                return truncateText(content, maxLength);
            
            case IMAGE:
                return "[图片]";
            
            case VOICE:
                Integer duration = mediaData != null ? (Integer) mediaData.get("duration") : null;
                return duration != null ? String.format("[语音] %d\"", duration) : "[语音]";
            
            case VIDEO:
                Integer videoDuration = mediaData != null ? (Integer) mediaData.get("duration") : null;
                return videoDuration != null ? String.format("[视频] %d\"", videoDuration) : "[视频]";
            
            case FILE:
                String fileName = content != null && !content.startsWith("[") ? content : "未知文件";
                return "[文件] " + truncateText(fileName, 20);
            
            case SYSTEM:
                return truncateText(content, maxLength);
            
            case EMOJI:
                return "[表情]";
            
            case LOCATION:
                String address = mediaData != null ? (String) mediaData.get("address") : null;
                return address != null ? "[位置] " + truncateText(address, 20) : "[位置]";
            
            case ORDER_CARD:
                return "[订单卡片]";
            
            default:
                return "[未知消息]";
        }
    }

    /**
     * 截断文本
     * 
     * @param text 原文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 计算消息大小（字节）
     * 
     * @param content 文本内容
     * @param mediaSize 媒体大小
     * @return 总大小（字节）
     */
    public static Long calculateMessageSize(String content, Long mediaSize) {
        long size = 0;
        
        if (content != null) {
            size += content.getBytes().length;
        }
        
        if (mediaSize != null) {
            size += mediaSize;
        }
        
        return size;
    }

    /**
     * 验证消息内容长度
     * 
     * @param messageType 消息类型
     * @param content 内容
     * @return 是否合法
     */
    public static boolean validateContentLength(Integer messageType, String content) {
        if (content == null) {
            return false;
        }

        ChatMessage.MessageType type = ChatMessage.MessageType.fromCode(messageType);
        if (type == null) {
            return false;
        }

        switch (type) {
            case TEXT:
                return content.length() <= 5000; // 文本消息最大5000字符
            case SYSTEM:
                return content.length() <= 1000; // 系统消息最大1000字符
            default:
                return true;
        }
    }

    /**
     * 验证媒体文件大小
     * 
     * @param messageType 消息类型
     * @param fileSize 文件大小（字节）
     * @return 是否合法
     */
    public static boolean validateMediaSize(Integer messageType, Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return false;
        }

        ChatMessage.MessageType type = ChatMessage.MessageType.fromCode(messageType);
        if (type == null) {
            return false;
        }

        switch (type) {
            case IMAGE:
                return fileSize <= 10 * 1024 * 1024; // 图片最大10MB
            case VOICE:
                return fileSize <= 5 * 1024 * 1024;  // 语音最大5MB
            case VIDEO:
                return fileSize <= 50 * 1024 * 1024; // 视频最大50MB
            case FILE:
                return fileSize <= 20 * 1024 * 1024; // 文件最大20MB
            default:
                return true;
        }
    }

    /**
     * 验证媒体时长
     * 
     * @param messageType 消息类型
     * @param duration 时长（秒）
     * @return 是否合法
     */
    public static boolean validateMediaDuration(Integer messageType, Integer duration) {
        if (duration == null || duration <= 0) {
            return false;
        }

        ChatMessage.MessageType type = ChatMessage.MessageType.fromCode(messageType);
        if (type == null) {
            return false;
        }

        switch (type) {
            case VOICE:
                return duration <= 60;   // 语音最长60秒
            case VIDEO:
                return duration <= 300;  // 视频最长5分钟
            default:
                return true;
        }
    }

    /**
     * 格式化时长显示
     * 
     * @param seconds 秒数
     * @return 格式化字符串（如：1:23）
     */
    public static String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "0:00";
        }

        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    /**
     * 格式化文件大小
     * 
     * @param bytes 字节数
     * @return 格式化字符串（如：1.5MB）
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0B";
        }

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f%s", size, units[unitIndex]);
    }

    /**
     * 检查消息是否可以撤回
     * 
     * @param senderId 发送者ID
     * @param currentUserId 当前用户ID
     * @param sendTime 发送时间
     * @param recallMinutes 可撤回时限（分钟）
     * @return 是否可以撤回
     */
    public static boolean canRecall(Long senderId, Long currentUserId, 
                                   java.time.LocalDateTime sendTime, int recallMinutes) {
        if (senderId == null || currentUserId == null || sendTime == null) {
            return false;
        }

        // 只有发送者可以撤回
        if (!senderId.equals(currentUserId)) {
            return false;
        }

        // 检查时间限制
        java.time.LocalDateTime deadline = java.time.LocalDateTime.now().minusMinutes(recallMinutes);
        return sendTime.isAfter(deadline);
    }
}

