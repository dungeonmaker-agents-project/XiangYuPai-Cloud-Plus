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
 * 用户消息设置实体(v7.1新增)
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_settings")
public class MessageSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设置记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID(唯一)
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    // ========== 推送设置 (7字段) ==========
    
    /**
     * 推送总开关
     */
    @TableField("push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;

    /**
     * 推送声音开关
     */
    @TableField("push_sound_enabled")
    @Builder.Default
    private Boolean pushSoundEnabled = true;

    /**
     * 推送震动开关
     */
    @TableField("push_vibrate_enabled")
    @Builder.Default
    private Boolean pushVibrateEnabled = true;

    /**
     * 推送内容预览开关(关闭则只显示"您有新消息")
     */
    @TableField("push_preview_enabled")
    @Builder.Default
    private Boolean pushPreviewEnabled = true;

    /**
     * 推送时段开始时间(如"08:00")
     */
    @TableField("push_start_time")
    @Builder.Default
    private String pushStartTime = "08:00";

    /**
     * 推送时段结束时间(如"22:00")
     */
    @TableField("push_end_time")
    @Builder.Default
    private String pushEndTime = "22:00";

    // ========== 分类推送开关 (4字段) ==========
    
    /**
     * 点赞消息推送开关
     */
    @TableField("push_like_enabled")
    @Builder.Default
    private Boolean pushLikeEnabled = true;

    /**
     * 评论消息推送开关
     */
    @TableField("push_comment_enabled")
    @Builder.Default
    private Boolean pushCommentEnabled = true;

    /**
     * 关注消息推送开关
     */
    @TableField("push_follow_enabled")
    @Builder.Default
    private Boolean pushFollowEnabled = true;

    /**
     * 系统通知推送开关
     */
    @TableField("push_system_enabled")
    @Builder.Default
    private Boolean pushSystemEnabled = true;

    // ========== 隐私设置 (2字段) ==========
    
    /**
     * 谁可以给我发消息(0=所有人,1=我关注的人,2=互相关注,3=不允许)
     */
    @TableField("who_can_message")
    @Builder.Default
    private Integer whoCanMessage = 0;

    /**
     * 谁可以添加我为好友(0=所有人,1=需要验证,2=不允许)
     */
    @TableField("who_can_add_friend")
    @Builder.Default
    private Integer whoCanAddFriend = 0;

    // ========== 消息设置 (2字段) ==========
    
    /**
     * 消息已读回执开关(关闭后不发送已读状态)
     */
    @TableField("message_read_receipt")
    @Builder.Default
    private Boolean messageReadReceipt = true;

    /**
     * 在线状态可见(关闭则显示为隐身)
     */
    @TableField("online_status_visible")
    @Builder.Default
    private Boolean onlineStatusVisible = true;

    // ========== 自动下载设置 (3字段) ==========
    
    /**
     * 自动下载图片(0=永不,1=仅WIFI,2=始终)
     */
    @TableField("auto_download_image")
    @Builder.Default
    private Integer autoDownloadImage = 2;

    /**
     * 自动下载视频(0=永不,1=仅WIFI,2=始终)
     */
    @TableField("auto_download_video")
    @Builder.Default
    private Integer autoDownloadVideo = 1;

    /**
     * 自动播放语音消息
     */
    @TableField("auto_play_voice")
    @Builder.Default
    private Boolean autoPlayVoice = false;

    // ========== 其他 (1字段) ==========
    
    /**
     * 消息保存天数(0=永久,7/30/90天自动清理)
     */
    @TableField("message_retention_days")
    @Builder.Default
    private Integer messageRetentionDays = 0;

    // ========== 时间字段 ==========
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== 枚举定义 ==========
    
    /**
     * 谁可以发消息枚举
     */
    public enum WhoCanMessage {
        EVERYONE(0, "所有人"),
        FOLLOWING(1, "我关注的人"),
        MUTUAL_FOLLOWING(2, "互相关注"),
        NO_ONE(3, "不允许");

        private final Integer code;
        private final String desc;

        WhoCanMessage(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static WhoCanMessage fromCode(Integer code) {
            for (WhoCanMessage value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 谁可以添加好友枚举
     */
    public enum WhoCanAddFriend {
        EVERYONE(0, "所有人"),
        NEED_VERIFY(1, "需要验证"),
        NO_ONE(2, "不允许");

        private final Integer code;
        private final String desc;

        WhoCanAddFriend(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static WhoCanAddFriend fromCode(Integer code) {
            for (WhoCanAddFriend value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 自动下载枚举
     */
    public enum AutoDownload {
        NEVER(0, "永不"),
        WIFI_ONLY(1, "仅WIFI"),
        ALWAYS(2, "始终");

        private final Integer code;
        private final String desc;

        AutoDownload(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static AutoDownload fromCode(Integer code) {
            for (AutoDownload value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    // ========== 业务方法 ==========
    
    /**
     * 是否允许推送
     */
    public boolean canPush() {
        return Boolean.TRUE.equals(pushEnabled);
    }

    /**
     * 是否在推送时段内
     */
    public boolean isInPushTimeRange() {
        if (!canPush()) {
            return false;
        }
        // TODO: 实现时间段判断逻辑
        return true;
    }

    /**
     * 是否允许某人发消息
     */
    public boolean canUserSendMessage(boolean isFollowing, boolean isMutualFollowing) {
        if (whoCanMessage == null) {
            return true;
        }
        
        WhoCanMessage rule = WhoCanMessage.fromCode(whoCanMessage);
        if (rule == null) {
            return true;
        }

        switch (rule) {
            case EVERYONE:
                return true;
            case FOLLOWING:
                return isFollowing;
            case MUTUAL_FOLLOWING:
                return isMutualFollowing;
            case NO_ONE:
                return false;
            default:
                return true;
        }
    }

    /**
     * 是否需要好友验证
     */
    public boolean needFriendVerify() {
        return WhoCanAddFriend.NEED_VERIFY.getCode().equals(whoCanAddFriend);
    }

    /**
     * 是否允许添加好友
     */
    public boolean canAddFriend() {
        return !WhoCanAddFriend.NO_ONE.getCode().equals(whoCanAddFriend);
    }

    /**
     * 是否显示已读回执
     */
    public boolean showReadReceipt() {
        return Boolean.TRUE.equals(messageReadReceipt);
    }

    /**
     * 是否显示在线状态
     */
    public boolean showOnlineStatus() {
        return Boolean.TRUE.equals(onlineStatusVisible);
    }

    /**
     * 是否自动下载图片
     */
    public boolean shouldAutoDownloadImage(boolean isWifi) {
        if (autoDownloadImage == null) {
            return isWifi;
        }
        
        AutoDownload rule = AutoDownload.fromCode(autoDownloadImage);
        if (rule == null) {
            return isWifi;
        }

        switch (rule) {
            case NEVER:
                return false;
            case WIFI_ONLY:
                return isWifi;
            case ALWAYS:
                return true;
            default:
                return isWifi;
        }
    }

    /**
     * 是否自动下载视频
     */
    public boolean shouldAutoDownloadVideo(boolean isWifi) {
        if (autoDownloadVideo == null) {
            return false;
        }
        
        AutoDownload rule = AutoDownload.fromCode(autoDownloadVideo);
        if (rule == null) {
            return false;
        }

        switch (rule) {
            case NEVER:
                return false;
            case WIFI_ONLY:
                return isWifi;
            case ALWAYS:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取谁可以发消息描述
     */
    public String getWhoCanMessageDesc() {
        WhoCanMessage rule = WhoCanMessage.fromCode(this.whoCanMessage);
        return rule != null ? rule.getDesc() : "所有人";
    }

    /**
     * 获取谁可以添加好友描述
     */
    public String getWhoCanAddFriendDesc() {
        WhoCanAddFriend rule = WhoCanAddFriend.fromCode(this.whoCanAddFriend);
        return rule != null ? rule.getDesc() : "所有人";
    }

    /**
     * 获取自动下载图片描述
     */
    public String getAutoDownloadImageDesc() {
        AutoDownload rule = AutoDownload.fromCode(this.autoDownloadImage);
        return rule != null ? rule.getDesc() : "仅WIFI";
    }

    /**
     * 获取自动下载视频描述
     */
    public String getAutoDownloadVideoDesc() {
        AutoDownload rule = AutoDownload.fromCode(this.autoDownloadVideo);
        return rule != null ? rule.getDesc() : "仅WIFI";
    }
}

