package com.xypai.chat.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 消息发送DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息发送DTO")
public class MessageSendDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属会话ID
     */
    @Schema(description = "所属会话ID", required = true)
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 消息类型(1=文本,2=图片,3=语音,4=视频,5=文件,6=系统通知,7=表情,8=位置)
     */
    @Schema(description = "消息类型", required = true)
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 消息内容(文本/文件名/系统通知文本)
     */
    @Schema(description = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 媒体文件URL
     */
    @Schema(description = "媒体文件URL")
    private String mediaUrl;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 媒体时长(秒)
     */
    @Schema(description = "媒体时长")
    private Integer duration;

    /**
     * 缩略图URL
     */
    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    /**
     * 回复的消息ID(引用回复)
     */
    @Schema(description = "回复的消息ID")
    private Long replyToId;

    // ========== v7.1新增字段 ==========
    
    /**
     * 客户端消息ID(用于消息去重)
     */
    @Schema(description = "客户端消息ID(UUID)")
    private String clientId;

    /**
     * 媒体宽度(像素)
     */
    @Schema(description = "媒体宽度")
    private Integer mediaWidth;

    /**
     * 媒体高度(像素)
     */
    @Schema(description = "媒体高度")
    private Integer mediaHeight;

    /**
     * 媒体配文(图片/视频说明)
     */
    @Schema(description = "媒体配文")
    private String mediaCaption;

    /**
     * 位置经度
     */
    @Schema(description = "位置经度")
    private Double longitude;

    /**
     * 位置纬度
     */
    @Schema(description = "位置纬度")
    private Double latitude;

    /**
     * 位置地址
     */
    @Schema(description = "位置地址")
    private String address;

    /**
     * 扩展媒体数据
     */
    @Schema(description = "扩展媒体数据")
    private Map<String, Object> extraMediaData;
}

