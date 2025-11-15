package org.dromara.common.notification.domain.bo;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知查询业务对象
 * Notification Query BO
 *
 * @author XiangYuPai Team
 */
@Data
public class NotificationQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知类型筛选 (可选)
     */
    private String type;

    /**
     * 已读状态筛选: 0=未读, 1=已读 (可选)
     */
    private Integer isRead;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer pageSize = 20;
}
