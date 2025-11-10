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
 * 会话查询DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会话查询DTO")
public class ConversationQueryDTO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话类型(1=私聊,2=群聊,3=系统通知,4=订单会话)
     */
    @Schema(description = "会话类型")
    private Integer type;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long creatorId;

    /**
     * 参与者ID(查询包含该用户的会话)
     */
    @Schema(description = "参与者ID")
    private Long participantId;

    /**
     * 会话状态(0=已解决,1=正常,2=已归档)
     */
    @Schema(description = "会话状态")
    private Integer status;

    /**
     * 会话标题(模糊搜索)
     */
    @Schema(description = "会话标题")
    private String title;

    /**
     * 关联订单ID
     */
    @Schema(description = "关联订单ID")
    private Long orderId;

    /**
     * 查询类型(my=我的会话, created=我创建的, joined=我参与的)
     */
    @Schema(description = "查询类型")
    private String queryType;

    /**
     * 是否包含最新消息
     */
    @Schema(description = "是否包含最新消息")
    private Boolean includeLatestMessage;

    /**
     * 是否包含未读数量
     */
    @Schema(description = "是否包含未读数量")
    private Boolean includeUnreadCount;
}

