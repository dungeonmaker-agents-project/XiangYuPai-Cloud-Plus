package org.dromara.common.notification.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.notification.domain.entity.Notification;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知创建业务对象
 * Notification Create BO
 *
 * @author XiangYuPai Team
 */
@Data
@AutoMapper(target = Notification.class, reverseConvertGenerate = false)
public class NotificationCreateBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接收用户ID
     */
    @NotNull(message = "接收用户ID不能为空")
    private Long userId;

    /**
     * 发送用户ID (系统通知可为null)
     */
    private Long fromUserId;

    /**
     * 通知类型
     */
    @NotBlank(message = "通知类型不能为空")
    private String type;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 扩展数据
     */
    private String extraData;
}
