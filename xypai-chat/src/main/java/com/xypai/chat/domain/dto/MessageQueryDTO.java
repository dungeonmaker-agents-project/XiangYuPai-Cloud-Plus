package com.xypai.chat.domain.dto;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 消息查询DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息查询DTO")
public class MessageQueryDTO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 消息类型(1=文本,2=图片,3=语音,4=视频,5=文件,6=系统通知,7=表情,8=位置)
     */
    @Schema(description = "消息类型")
    private Integer messageType;

    /**
     * 消息状态(0=已删除,1=正常,2=已撤回)
     */
    @Schema(description = "消息状态")
    private Integer status;

    /**
     * 消息内容(模糊搜索)
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 回复的消息ID
     */
    @Schema(description = "回复的消息ID")
    private Long replyToId;

    /**
     * 查询方向(before=向前查询, after=向后查询)
     */
    @Schema(description = "查询方向")
    private String direction;

    /**
     * 基准消息ID(用于分页)
     */
    @Schema(description = "基准消息ID")
    private Long baseMessageId;

    /**
     * 限制数量
     */
    @Schema(description = "限制数量")
    private Integer limit;

    /**
     * 是否包含发送者信息
     */
    @Schema(description = "是否包含发送者信息")
    private Boolean includeSender;

    /**
     * 是否包含回复消息信息
     */
    @Schema(description = "是否包含回复消息信息")
    private Boolean includeReplyTo;
}

