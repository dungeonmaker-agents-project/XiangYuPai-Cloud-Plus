package com.xypai.chat.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息设置VO
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSettingsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 推送时段开始时间
     */
    private String pushStartTime;

    /**
     * 推送时段结束时间
     */
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
     * 谁可以给我发消息
     */
    private Integer whoCanMessage;

    /**
     * 谁可以给我发消息描述
     */
    private String whoCanMessageDesc;

    /**
     * 谁可以添加我为好友
     */
    private Integer whoCanAddFriend;

    /**
     * 谁可以添加我为好友描述
     */
    private String whoCanAddFriendDesc;

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
     * 自动下载图片
     */
    private Integer autoDownloadImage;

    /**
     * 自动下载图片描述
     */
    private String autoDownloadImageDesc;

    /**
     * 自动下载视频
     */
    private Integer autoDownloadVideo;

    /**
     * 自动下载视频描述
     */
    private String autoDownloadVideoDesc;

    /**
     * 自动播放语音消息
     */
    private Boolean autoPlayVoice;

    // ========== 其他 ==========
    
    /**
     * 消息保存天数
     */
    private Integer messageRetentionDays;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

