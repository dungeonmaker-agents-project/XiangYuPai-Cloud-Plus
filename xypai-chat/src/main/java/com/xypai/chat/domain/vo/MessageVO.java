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
 * 消息VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息VO")
public class MessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID")
    private Long id;

    /**
     * 所属会话ID
     */
    @Schema(description = "所属会话ID")
    private Long conversationId;

    /**
     * 发送者ID
     */
    @Schema(description = "发送者ID")
    private Long senderId;

    /**
     * 发送者信息
     */
    @Schema(description = "发送者信息")
    private SenderInfoVO sender;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型")
    private Integer messageType;

    /**
     * 消息类型描述
     */
    @Schema(description = "消息类型描述")
    private String messageTypeDesc;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 媒体数据
     */
    @Schema(description = "媒体数据")
    private MediaDataVO mediaData;

    /**
     * 回复的消息ID
     */
    @Schema(description = "回复的消息ID")
    private Long replyToId;

    /**
     * 回复的消息信息
     */
    @Schema(description = "回复的消息信息")
    private ReplyToMessageVO replyToMessage;

    /**
     * 消息状态
     */
    @Schema(description = "消息状态")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 是否为系统消息
     */
    @Schema(description = "是否为系统消息")
    private Boolean isSystem;

    /**
     * 是否可以撤回
     */
    @Schema(description = "是否可以撤回")
    private Boolean canRecall;

    /**
     * 已读状态列表(群聊时显示)
     */
    @Schema(description = "已读状态列表")
    private List<ReadStatusVO> readStatus;

    // ========== v7.1新增字段 ==========
    
    /**
     * 客户端消息ID(v7.1消息去重)
     */
    @Schema(description = "客户端消息ID")
    private String clientId;

    /**
     * 消息序列号(v7.1消息有序性)
     */
    @Schema(description = "消息序列号")
    private Long sequenceId;

    /**
     * 投递状态(v7.1送达回执)
     */
    @Schema(description = "投递状态(0=发送中,1=已发送,2=已送达,3=已读,4=失败)")
    private Integer deliveryStatus;

    /**
     * 投递状态描述
     */
    @Schema(description = "投递状态描述")
    private String deliveryStatusDesc;

    /**
     * 已读人数(群聊使用)
     */
    @Schema(description = "已读人数")
    private Integer readCount;

    /**
     * 点赞数量
     */
    @Schema(description = "点赞数量")
    private Integer likeCount;

    /**
     * 是否已撤回
     */
    @Schema(description = "是否已撤回")
    private Boolean isRecalled;

    /**
     * 撤回操作人ID(v7.1)
     */
    @Schema(description = "撤回操作人ID")
    private Long recalledBy;

    /**
     * 撤回操作人昵称
     */
    @Schema(description = "撤回操作人昵称")
    private String recalledByNickname;

    /**
     * 客户端发送时间(v7.1时间分离)
     */
    @Schema(description = "客户端发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 服务器接收时间(v7.1时间分离)
     */
    @Schema(description = "服务器接收时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serverTime;

    /**
     * 消息序号(会话内递增) - 兼容旧字段
     */
    @Schema(description = "消息序号")
    private Long sequenceNumber;

    /**
     * 发送者信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "发送者信息VO")
    public static class SenderInfoVO implements Serializable {
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

        @Schema(description = "群昵称")
        private String groupNickname;

        @Schema(description = "角色")
        private String role;
    }

    /**
     * 媒体数据VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "媒体数据VO")
    public static class MediaDataVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "媒体URL")
        private String url;

        @Schema(description = "文件大小")
        private Long size;

        @Schema(description = "媒体时长")
        private Integer duration;

        @Schema(description = "缩略图URL")
        private String thumbnail;

        @Schema(description = "宽度")
        private Integer width;

        @Schema(description = "高度")
        private Integer height;

        @Schema(description = "位置经度")
        private Double longitude;

        @Schema(description = "位置纬度")
        private Double latitude;

        @Schema(description = "位置地址")
        private String address;

        @Schema(description = "文件格式")
        private String format;
    }

    /**
     * 回复消息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "回复消息VO")
    public static class ReplyToMessageVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "被回复消息ID")
        private Long messageId;

        @Schema(description = "被回复消息发送者ID")
        private Long senderId;

        @Schema(description = "被回复消息发送者昵称")
        private String senderNickname;

        @Schema(description = "被回复消息类型")
        private Integer messageType;

        @Schema(description = "被回复消息内容")
        private String content;

        @Schema(description = "被回复消息预览")
        private String preview;

        @Schema(description = "是否已被删除")
        private Boolean isDeleted;
    }

    /**
     * 已读状态VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "已读状态VO")
    public static class ReadStatusVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户昵称")
        private String nickname;

        @Schema(description = "用户头像")
        private String avatar;

        @Schema(description = "已读时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime readAt;

        @Schema(description = "是否已读")
        private Boolean isRead;
    }
}

