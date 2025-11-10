package com.xypai.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * 消息设置更新DTO
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSettingsUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 推送设置 ==========
    
    /**
     * 推送总开关
     */
    private Boolean pushEnabled;

    /**
     * 推送声音开关
     */
    private Boolean pushSoundEnabled;

    /**
     * 推送震动开关
     */
    private Boolean pushVibrateEnabled;

    /**
     * 推送内容预览开关
     */
    private Boolean pushPreviewEnabled;

    /**
     * 推送时段开始时间(如"08:00")
     */
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "时间格式错误,应为HH:mm格式")
    private String pushStartTime;

    /**
     * 推送时段结束时间(如"22:00")
     */
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "时间格式错误,应为HH:mm格式")
    private String pushEndTime;

    // ========== 分类推送开关 ==========
    
    /**
     * 点赞消息推送开关
     */
    private Boolean pushLikeEnabled;

    /**
     * 评论消息推送开关
     */
    private Boolean pushCommentEnabled;

    /**
     * 关注消息推送开关
     */
    private Boolean pushFollowEnabled;

    /**
     * 系统通知推送开关
     */
    private Boolean pushSystemEnabled;

    // ========== 隐私设置 ==========
    
    /**
     * 谁可以给我发消息(0=所有人,1=我关注的人,2=互相关注,3=不允许)
     */
    @Min(value = 0, message = "whoCanMessage值必须在0-3之间")
    @Max(value = 3, message = "whoCanMessage值必须在0-3之间")
    private Integer whoCanMessage;

    /**
     * 谁可以添加我为好友(0=所有人,1=需要验证,2=不允许)
     */
    @Min(value = 0, message = "whoCanAddFriend值必须在0-2之间")
    @Max(value = 2, message = "whoCanAddFriend值必须在0-2之间")
    private Integer whoCanAddFriend;

    // ========== 消息设置 ==========
    
    /**
     * 消息已读回执开关
     */
    private Boolean messageReadReceipt;

    /**
     * 在线状态可见
     */
    private Boolean onlineStatusVisible;

    // ========== 自动下载设置 ==========
    
    /**
     * 自动下载图片(0=永不,1=仅WIFI,2=始终)
     */
    @Min(value = 0, message = "autoDownloadImage值必须在0-2之间")
    @Max(value = 2, message = "autoDownloadImage值必须在0-2之间")
    private Integer autoDownloadImage;

    /**
     * 自动下载视频(0=永不,1=仅WIFI,2=始终)
     */
    @Min(value = 0, message = "autoDownloadVideo值必须在0-2之间")
    @Max(value = 2, message = "autoDownloadVideo值必须在0-2之间")
    private Integer autoDownloadVideo;

    /**
     * 自动播放语音消息
     */
    private Boolean autoPlayVoice;

    // ========== 其他 ==========
    
    /**
     * 消息保存天数(0=永久,7/30/90天自动清理)
     */
    @Min(value = 0, message = "messageRetentionDays值必须大于等于0")
    private Integer messageRetentionDays;
}

