package org.dromara.content.api.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 报名活动DTO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityRegisterDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 报名留言
     */
    private String message;
}
