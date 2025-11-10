package com.xypai.chat.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话列表VO")
public class ConversationListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private Long id;

    /**
     * 会话类型
     */
    @Schema(description = "会话类型")
    private Integer type;

    /**
     * 会话类型描述
     */
    @Schema(description = "会话类型描述")
    private String typeDesc;

    /**
     * 会话标题
     */
    @Schema(description = "会话标题")
    private String title;

    /**
     * 会话描述
     */
    @Schema(description = "会话描述")
    private String description;

    /**
     * 会话头像
     */
    @Schema(description = "会话头像")
    private String avatar;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long creatorId;

    /**
     * 创建者信息
     */
    @Schema(description = "创建者信息")
    private UserInfoVO creator;

    /**
     * 会话状态
     */
    @Schema(description = "会话状态")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 参与者数量
     */
    @Schema(description = "参与者数量")
    private Integer participantCount;

    /**
     * 最新消息
     */
    @Schema(description = "最新消息")
    private LatestMessageVO latestMessage;

    /**
     * 未读消息数量
     */
    @Schema(description = "未读消息数量")
    private Integer unreadCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== v7.1新增字段 ==========
    
    /**
     * 关联订单ID(订单会话使用)
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 最后消息ID(v7.1冗余字段)
     */
    @Schema(description = "最后消息ID")
    private Long lastMessageId;

    /**
     * 最后消息时间(v7.1冗余字段)
     */
    @Schema(description = "最后消息时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /**
     * 消息总数(v7.1冗余字段)
     */
    @Schema(description = "消息总数")
    private Integer totalMessageCount;

    /**
     * 是否置顶(v7.1个性化设置)
     */
    @Schema(description = "是否置顶")
    private Boolean isPinned;

    /**
     * 是否静音(v7.1个性化设置)
     */
    @Schema(description = "是否静音")
    private Boolean isMuted;

    /**
     * 免打扰截止时间(v7.1)
     */
    @Schema(description = "免打扰截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime muteUntil;

    /**
     * 用户在该会话的角色(owner=群主, admin=管理员, member=成员)
     */
    @Schema(description = "用户角色")
    private String userRole;

    /**
     * 用户在该会话的群昵称(v7.1)
     */
    @Schema(description = "群昵称")
    private String groupNickname;

    /**
     * 用户信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息VO")
    public static class UserInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "在线状态")
        private String onlineStatus;
    }

    /**
     * 最新消息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最新消息VO")
    public static class LatestMessageVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "消息ID")
        private Long messageId;

        @Schema(description = "发送者ID")
        private Long senderId;

        @Schema(description = "发送者昵称")
        private String senderNickname;

        @Schema(description = "消息类型")
        private Integer messageType;

        @Schema(description = "消息类型描述")
        private String messageTypeDesc;

        @Schema(description = "消息内容")
        private String content;

        @Schema(description = "消息预览内容")
        private String preview;

        @Schema(description = "发送时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @Schema(description = "是否为系统消息")
        private Boolean isSystem;
    }
}

