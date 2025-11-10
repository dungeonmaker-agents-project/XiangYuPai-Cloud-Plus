package com.xypai.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天消息实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属会话ID
     */
    @TableField("conversation_id")
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 发送者ID(NULL=系统消息)
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 消息类型(1=文本,2=图片,3=语音,4=视频,5=文件,6=系统通知)
     */
    @TableField("message_type")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 消息内容(文本/文件名/系统通知文本)
     */
    @TableField("content")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    // ========== v7.1新增：媒体字段展开 (7字段) ==========
    
    /**
     * 媒体文件URL(图片/语音/视频文件CDN地址)
     */
    @TableField("media_url")
    private String mediaUrl;

    /**
     * 缩略图URL(图片缩略图或视频封面)
     */
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 媒体文件大小(字节)
     */
    @TableField("media_size")
    private Long mediaSize;

    /**
     * 媒体宽度(像素,图片/视频使用)
     */
    @TableField("media_width")
    private Integer mediaWidth;

    /**
     * 媒体高度(像素,图片/视频使用)
     */
    @TableField("media_height")
    private Integer mediaHeight;

    /**
     * 媒体时长(秒,语音/视频使用,语音最长60s,视频最长5分钟)
     */
    @TableField("media_duration")
    private Integer mediaDuration;

    /**
     * 媒体配文(图片/视频的文字说明)
     */
    @TableField("media_caption")
    private String mediaCaption;

    /**
     * 媒体数据JSON{其他扩展字段...}
     * v7.1: 保留兼容旧数据,核心字段已展开
     */
    @TableField(value = "media_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> mediaData;

    /**
     * 回复的消息ID(引用回复功能)
     */
    @TableField("reply_to_id")
    private Long replyToId;

    // ========== v7.1新增：消息管理字段 (3字段) ⚠️ 核心功能 ==========
    
    /**
     * 客户端消息ID(用于消息去重和状态追踪)
     */
    @TableField("client_id")
    private String clientId;

    /**
     * 消息序列号(保证消息有序性)
     */
    @TableField("sequence_id")
    private Long sequenceId;

    /**
     * 投递状态(0=发送中,1=已发送,2=已送达,3=已读,4=发送失败)
     */
    @TableField("delivery_status")
    @Builder.Default
    private Integer deliveryStatus = 0;

    // ========== v7.1新增：群聊功能增强 (3字段) ==========
    
    /**
     * 已读人数(群聊使用)
     */
    @TableField("read_count")
    @Builder.Default
    private Integer readCount = 0;

    /**
     * 点赞数量(图片消息支持点赞)
     */
    @TableField("like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 撤回操作人ID(群聊场景需要知道谁撤回的)
     */
    @TableField("recalled_by")
    private Long recalledBy;

    /**
     * 消息状态(0=已删除,1=正常,2=已撤回,3=审核中)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    // ========== v7.1新增：时间分离 (3字段) ==========
    
    /**
     * 客户端发送时间
     */
    @TableField("send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 服务器接收时间
     */
    @TableField("server_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serverTime;

    /**
     * 软删除时间(NULL=正常,NOT NULL=已删除)
     */
    @TableField("deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== v7.1新增：投递状态枚举 ==========
    
    /**
     * 投递状态枚举
     */
    public enum DeliveryStatus {
        SENDING(0, "发送中"),
        SENT(1, "已发送"),
        DELIVERED(2, "已送达"),
        READ(3, "已读"),
        FAILED(4, "发送失败");

        private final Integer code;
        private final String desc;

        DeliveryStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static DeliveryStatus fromCode(Integer code) {
            for (DeliveryStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT(1, "文本"),
        IMAGE(2, "图片"),
        VOICE(3, "语音"),
        VIDEO(4, "视频"),
        FILE(5, "文件"),
        SYSTEM(6, "系统通知"),
        EMOJI(7, "表情"),
        LOCATION(8, "位置"),
        ORDER_CARD(9, "订单卡片");

        private final Integer code;
        private final String desc;

        MessageType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static MessageType fromCode(Integer code) {
            for (MessageType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 消息状态枚举
     */
    public enum Status {
        DELETED(0, "已删除"),
        NORMAL(1, "正常"),
        RECALLED(2, "已撤回");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 是否为文本消息
     */
    public boolean isText() {
        return MessageType.TEXT.getCode().equals(this.messageType);
    }

    /**
     * 是否为图片消息
     */
    public boolean isImage() {
        return MessageType.IMAGE.getCode().equals(this.messageType);
    }

    /**
     * 是否为语音消息
     */
    public boolean isVoice() {
        return MessageType.VOICE.getCode().equals(this.messageType);
    }

    /**
     * 是否为视频消息
     */
    public boolean isVideo() {
        return MessageType.VIDEO.getCode().equals(this.messageType);
    }

    /**
     * 是否为文件消息
     */
    public boolean isFile() {
        return MessageType.FILE.getCode().equals(this.messageType);
    }

    /**
     * 是否为系统消息
     */
    public boolean isSystem() {
        return MessageType.SYSTEM.getCode().equals(this.messageType) || senderId == null;
    }

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return Status.DELETED.getCode().equals(this.status);
    }

    /**
     * 是否已撤回
     */
    public boolean isRecalled() {
        return Status.RECALLED.getCode().equals(this.status);
    }

    /**
     * 是否为回复消息
     */
    public boolean isReply() {
        return replyToId != null;
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDesc() {
        MessageType type = MessageType.fromCode(this.messageType);
        return type != null ? type.getDesc() : "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status messageStatus = Status.fromCode(this.status);
        return messageStatus != null ? messageStatus.getDesc() : "未知";
    }

    // ========== v7.1兼容方法：优先使用新字段 ==========
    
    /**
     * 获取媒体URL(v7.1: 优先使用mediaUrl字段)
     */
    public String getMediaUrlCompat() {
        if (mediaUrl != null) {
            return mediaUrl;
        }
        return mediaData != null ? (String) mediaData.get("url") : null;
    }

    /**
     * 获取文件大小(v7.1: 优先使用mediaSize字段)
     */
    public Long getFileSize() {
        if (mediaSize != null) {
            return mediaSize;
        }
        if (mediaData != null && mediaData.get("size") != null) {
            return ((Number) mediaData.get("size")).longValue();
        }
        return null;
    }

    /**
     * 获取媒体时长(v7.1: 优先使用mediaDuration字段)
     */
    public Integer getDuration() {
        if (mediaDuration != null) {
            return mediaDuration;
        }
        return mediaData != null ? (Integer) mediaData.get("duration") : null;
    }

    /**
     * 获取缩略图URL(v7.1: 优先使用thumbnailUrl字段)
     */
    public String getThumbnailUrlCompat() {
        if (thumbnailUrl != null) {
            return thumbnailUrl;
        }
        return mediaData != null ? (String) mediaData.get("thumbnail") : null;
    }

    /**
     * 检查是否可以撤回(v7.1: 2分钟内)
     */
    public boolean canRecall(Long currentUserId) {
        // 只有发送者可以撤回，且在2分钟内
        return senderId != null && senderId.equals(currentUserId) && 
               createdAt != null && createdAt.isAfter(LocalDateTime.now().minusMinutes(2)) &&
               isNormal();
    }

    // ========== v7.1新增：投递状态判断 ==========
    
    /**
     * 是否发送中
     */
    public boolean isSending() {
        return DeliveryStatus.SENDING.getCode().equals(this.deliveryStatus);
    }

    /**
     * 是否已发送
     */
    public boolean isSent() {
        return DeliveryStatus.SENT.getCode().equals(this.deliveryStatus);
    }

    /**
     * 是否已送达
     */
    public boolean isDelivered() {
        return DeliveryStatus.DELIVERED.getCode().equals(this.deliveryStatus);
    }

    /**
     * 是否已读
     */
    public boolean isRead() {
        return DeliveryStatus.READ.getCode().equals(this.deliveryStatus);
    }

    /**
     * 是否发送失败
     */
    public boolean isFailed() {
        return DeliveryStatus.FAILED.getCode().equals(this.deliveryStatus);
    }

    /**
     * 获取投递状态描述
     */
    public String getDeliveryStatusDesc() {
        DeliveryStatus status = DeliveryStatus.fromCode(this.deliveryStatus);
        return status != null ? status.getDesc() : "未知";
    }

    /**
     * 检查消息是否需要去重(基于clientId)
     */
    public boolean isDuplicate(String checkClientId) {
        return this.clientId != null && this.clientId.equals(checkClientId);
    }
}

