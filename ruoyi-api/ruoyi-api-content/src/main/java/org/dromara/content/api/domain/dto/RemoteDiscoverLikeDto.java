package org.dromara.content.api.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发现页点赞请求DTO
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
public class RemoteDiscoverLikeDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID（动态ID）
     */
    private Long contentId;

    /**
     * 操作类型: like(点赞), unlike(取消点赞)
     */
    private String action;

    /**
     * 是否为点赞操作
     */
    public boolean isLikeAction() {
        return "like".equalsIgnoreCase(action);
    }
}
