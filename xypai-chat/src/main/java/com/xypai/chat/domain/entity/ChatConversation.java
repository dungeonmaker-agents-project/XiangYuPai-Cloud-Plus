package com.xypai.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天会话实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "chat_conversation", autoResultMap = true)
public class ChatConversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话唯一ID(雪花ID)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 会话类型(1=私聊,2=群聊,3=系统通知)
     */
    @TableField("type")
    @NotNull(message = "会话类型不能为空")
    private Integer type;

    /**
     * 会话标题(群聊名称,私聊可为空)
     */
    @TableField("title")
    @Size(max = 100, message = "会话标题长度不能超过100个字符")
    private String title;

    /**
     * 创建者ID(群主/发起人)
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 会话头像URL(群聊头像或对方用户头像)
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 会话描述(群聊公告等)
     */
    @TableField("description")
    private String description;

    /**
     * 关联订单ID(订单会话使用)
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 最后一条消息ID(冗余优化,提升列表查询性能)
     */
    @TableField("last_message_id")
    private Long lastMessageId;

    /**
     * 最后消息时间(用于对话列表排序)
     */
    @TableField("last_message_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /**
     * 消息总数(冗余统计)
     */
    @TableField("total_message_count")
    @Builder.Default
    private Integer totalMessageCount = 0;

    /**
     * 成员数量(群聊使用)
     */
    @TableField("member_count")
    @Builder.Default
    private Integer memberCount = 0;

    /**
     * 扩展信息JSON{其他扩展字段...}
     * v7.1: 保留兼容旧数据,核心字段已展开
     */
    @TableField(value = "metadata", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 会话状态(0=已解散,1=正常,2=已归档)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Builder.Default
    private Integer version = 0;

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

    /**
     * 最后活跃时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 会话类型枚举
     */
    public enum Type {
        PRIVATE(1, "私聊"),
        GROUP(2, "群聊"),
        SYSTEM(3, "系统通知"),
        ORDER(4, "订单会话");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Type fromCode(Integer code) {
            for (Type type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 会话状态枚举
     */
    public enum Status {
        DISSOLVED(0, "已解散"),
        NORMAL(1, "正常"),
        ARCHIVED(2, "已归档");

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
     * 是否为私聊
     */
    public boolean isPrivate() {
        return Type.PRIVATE.getCode().equals(this.type);
    }

    /**
     * 是否为群聊
     */
    public boolean isGroup() {
        return Type.GROUP.getCode().equals(this.type);
    }

    /**
     * 是否为系统通知
     */
    public boolean isSystem() {
        return Type.SYSTEM.getCode().equals(this.type);
    }

    /**
     * 是否为订单会话
     */
    public boolean isOrder() {
        return Type.ORDER.getCode().equals(this.type);
    }

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 是否已解散
     */
    public boolean isDissolved() {
        return Status.DISSOLVED.getCode().equals(this.status);
    }

    /**
     * 是否已归档
     */
    public boolean isArchived() {
        return Status.ARCHIVED.getCode().equals(this.status);
    }

    /**
     * 获取会话类型描述
     */
    public String getTypeDesc() {
        Type conversationType = Type.fromCode(this.type);
        return conversationType != null ? conversationType.getDesc() : "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status conversationStatus = Status.fromCode(this.status);
        return conversationStatus != null ? conversationStatus.getDesc() : "未知";
    }

    /**
     * 获取会话头像(v7.1: 优先使用avatarUrl字段)
     */
    public String getAvatar() {
        if (avatarUrl != null) {
            return avatarUrl;
        }
        return metadata != null ? (String) metadata.get("avatar") : null;
    }

    /**
     * 设置会话头像(v7.1: 同时更新avatarUrl字段)
     */
    public void setAvatar(String avatar) {
        this.avatarUrl = avatar;
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("avatar", avatar);
    }

    /**
     * 获取最大成员数
     */
    public Integer getMaxMembers() {
        return metadata != null ? (Integer) metadata.get("max_members") : null;
    }

    /**
     * 设置最大成员数
     */
    public void setMaxMembers(Integer maxMembers) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("max_members", maxMembers);
    }

    /**
     * 检查是否允许邀请
     */
    public boolean isInviteEnabled() {
        return metadata != null ? 
            Boolean.TRUE.equals(metadata.get("invite_enabled")) : true;
    }

    /**
     * 设置是否允许邀请
     */
    public void setInviteEnabled(boolean inviteEnabled) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("invite_enabled", inviteEnabled);
    }
}
