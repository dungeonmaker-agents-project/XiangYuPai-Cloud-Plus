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
import java.util.List;
import java.util.Map;

/**
 * 会话详情VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话详情VO")
public class ConversationDetailVO implements Serializable {

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
    private ConversationListVO.UserInfoVO creator;

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
     * 最大成员数
     */
    @Schema(description = "最大成员数")
    private Integer maxMembers;

    /**
     * 是否允许邀请
     */
    @Schema(description = "是否允许邀请")
    private Boolean inviteEnabled;

    /**
     * 参与者列表
     */
    @Schema(description = "参与者列表")
    private List<ParticipantVO> participants;

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

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 当前用户在该会话的权限
     */
    @Schema(description = "当前用户权限")
    private UserPermissionVO userPermission;

    /**
     * 会话设置
     */
    @Schema(description = "会话设置")
    private ConversationSettingsVO settings;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private Map<String, Object> metadata;

    /**
     * 参与者VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "参与者VO")
    public static class ParticipantVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "参与者ID")
        private Long participantId;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "角色")
        private String role;

        @Schema(description = "角色描述")
        private String roleDesc;

        @Schema(description = "加入时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime joinedAt;

        @Schema(description = "最后阅读时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastReadAt;

        @Schema(description = "在线状态")
        private String onlineStatus;

        @Schema(description = "是否静音")
        private Boolean isMuted;

        @Schema(description = "是否置顶")
        private Boolean isPinned;
    }

    /**
     * 用户权限VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户权限VO")
    public static class UserPermissionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "是否可以发送消息")
        private Boolean canSendMessage;

        @Schema(description = "是否可以邀请成员")
        private Boolean canInvite;

        @Schema(description = "是否可以移除成员")
        private Boolean canRemove;

        @Schema(description = "是否可以修改会话信息")
        private Boolean canModify;

        @Schema(description = "是否可以解散会话")
        private Boolean canDissolve;

        @Schema(description = "是否可以退出会话")
        private Boolean canQuit;

        @Schema(description = "角色类型")
        private String role;
    }

    /**
     * 会话设置VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "会话设置VO")
    public static class ConversationSettingsVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "是否静音")
        private Boolean isMuted;

        @Schema(description = "是否置顶")
        private Boolean isPinned;

        @Schema(description = "消息保存天数")
        private Integer messageRetentionDays;

        @Schema(description = "是否允许成员邀请")
        private Boolean allowMemberInvite;

        @Schema(description = "是否开启已读回执")
        private Boolean readReceiptEnabled;

        @Schema(description = "是否开启群昵称")
        private Boolean groupNicknameEnabled;
    }
}

