package com.xypai.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话参与者实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_participant")
public class ChatParticipant implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参与记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 参与用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色权限(1=成员,2=管理员,3=群主)
     */
    @TableField("role")
    @Builder.Default
    private Integer role = 1;

    /**
     * 加入时间
     */
    @TableField(value = "join_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /**
     * 最后已读时间(未读消息计算)
     */
    @TableField("last_read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReadTime;

    // ========== v7.1新增：精确已读定位 (2字段) ==========
    
    /**
     * 最后已读消息ID(精确定位已读位置)
     */
    @TableField("last_read_message_id")
    private Long lastReadMessageId;

    /**
     * 未读消息数量(冗余优化,实时更新)
     */
    @TableField("unread_count")
    @Builder.Default
    private Integer unreadCount = 0;

    // ========== v7.1新增：个性化设置 (4字段) ==========
    
    /**
     * 是否置顶此会话(用户个性化设置)
     */
    @TableField("is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    /**
     * 是否免打扰(不接收推送通知)
     */
    @TableField("is_muted")
    @Builder.Default
    private Boolean isMuted = false;

    /**
     * 免打扰截止时间(NULL=永久免打扰)
     */
    @TableField("mute_until")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime muteUntil;

    /**
     * 群聊中的昵称(群昵称,可选)
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 参与状态(0=已退出,1=正常,2=已禁言)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 退出时间
     */
    @TableField("leave_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;

    /**
     * 角色枚举
     */
    public enum Role {
        MEMBER(1, "成员"),
        ADMIN(2, "管理员"),
        OWNER(3, "群主");

        private final Integer code;
        private final String desc;

        Role(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Role fromCode(Integer code) {
            for (Role role : values()) {
                if (role.getCode().equals(code)) {
                    return role;
                }
            }
            return null;
        }
    }

    /**
     * 参与状态枚举
     */
    public enum Status {
        LEFT(0, "已退出"),
        NORMAL(1, "正常"),
        MUTED(2, "已禁言");

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
     * 是否为普通成员
     */
    public boolean isMember() {
        return Role.MEMBER.getCode().equals(this.role);
    }

    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return Role.ADMIN.getCode().equals(this.role);
    }

    /**
     * 是否为群主
     */
    public boolean isOwner() {
        return Role.OWNER.getCode().equals(this.role);
    }

    /**
     * 是否有管理权限(管理员或群主)
     */
    public boolean hasAdminPermission() {
        return isAdmin() || isOwner();
    }

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 是否已退出
     */
    public boolean isLeft() {
        return Status.LEFT.getCode().equals(this.status);
    }

    /**
     * 是否被禁言
     */
    public boolean isMuted() {
        return Status.MUTED.getCode().equals(this.status);
    }

    /**
     * 是否可以发言
     */
    public boolean canSpeak() {
        return isNormal() && !isMuted();
    }

    /**
     * 是否可以邀请成员
     */
    public boolean canInvite() {
        return hasAdminPermission() && isNormal();
    }

    /**
     * 是否可以踢出成员
     */
    public boolean canKickMember() {
        return hasAdminPermission() && isNormal();
    }

    /**
     * 是否可以禁言成员
     */
    public boolean canMuteMember() {
        return hasAdminPermission() && isNormal();
    }

    /**
     * 获取角色描述
     */
    public String getRoleDesc() {
        Role participantRole = Role.fromCode(this.role);
        return participantRole != null ? participantRole.getDesc() : "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        Status participantStatus = Status.fromCode(this.status);
        return participantStatus != null ? participantStatus.getDesc() : "未知";
    }

    /**
     * 更新最后已读时间
     */
    public void updateLastReadTime() {
        this.lastReadTime = LocalDateTime.now();
    }

    /**
     * 检查是否有新消息(基于最后已读时间)
     */
    public boolean hasUnreadMessages(LocalDateTime latestMessageTime) {
        return lastReadTime == null || 
               (latestMessageTime != null && latestMessageTime.isAfter(lastReadTime));
    }

    // ========== v7.1新增：个性化设置相关方法 ==========
    
    /**
     * 检查是否免打扰中
     */
    public boolean isCurrentlyMuted() {
        if (!Boolean.TRUE.equals(isMuted)) {
            return false;
        }
        // 如果设置了截止时间，检查是否已过期
        if (muteUntil != null) {
            return muteUntil.isAfter(LocalDateTime.now());
        }
        // 永久免打扰
        return true;
    }

    /**
     * 设置永久免打扰
     */
    public void setMuteForever() {
        this.isMuted = true;
        this.muteUntil = null;
    }

    /**
     * 设置定时免打扰
     */
    public void setMuteUntil(LocalDateTime until) {
        this.isMuted = true;
        this.muteUntil = until;
    }

    /**
     * 取消免打扰
     */
    public void unmute() {
        this.isMuted = false;
        this.muteUntil = null;
    }

    /**
     * 增加未读数量
     */
    public void incrementUnreadCount() {
        if (this.unreadCount == null) {
            this.unreadCount = 1;
        } else {
            this.unreadCount++;
        }
    }

    /**
     * 清空未读数量
     */
    public void clearUnreadCount() {
        this.unreadCount = 0;
    }

    /**
     * 更新已读位置(v7.1: 同时记录消息ID和时间)
     */
    public void updateReadPosition(Long messageId, LocalDateTime readTime) {
        this.lastReadMessageId = messageId;
        this.lastReadTime = readTime;
        this.clearUnreadCount();
    }
}

