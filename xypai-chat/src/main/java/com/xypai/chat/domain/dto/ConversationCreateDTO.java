package com.xypai.chat.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 会话创建DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话创建DTO")
public class ConversationCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话类型(1=私聊,2=群聊,3=系统通知,4=订单会话)
     */
    @Schema(description = "会话类型", required = true)
    @NotNull(message = "会话类型不能为空")
    private Integer type;

    /**
     * 会话标题(群聊名称,私聊可为空)
     */
    @Schema(description = "会话标题")
    @Size(max = 100, message = "会话标题长度不能超过100个字符")
    private String title;

    /**
     * 会话描述
     */
    @Schema(description = "会话描述")
    @Size(max = 500, message = "会话描述长度不能超过500个字符")
    private String description;

    /**
     * 会话头像
     */
    @Schema(description = "会话头像URL")
    private String avatar;

    /**
     * 参与者用户ID列表(不包含创建者)
     */
    @Schema(description = "参与者用户ID列表")
    private List<Long> participantIds;

    /**
     * 最大成员数(群聊)
     */
    @Schema(description = "最大成员数")
    private Integer maxMembers;

    /**
     * 是否允许邀请
     */
    @Schema(description = "是否允许邀请")
    private Boolean inviteEnabled;

    /**
     * 关联订单ID(订单会话)
     */
    @Schema(description = "关联订单ID")
    private Long orderId;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private Map<String, Object> extraData;
}

