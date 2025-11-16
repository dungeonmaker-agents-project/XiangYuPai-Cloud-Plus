package org.dromara.common.notification.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 通知实体
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 发送用户ID
     */
    private Long fromUserId;

    /**
     * 通知类型 (like/comment/follow/system/activity)
     */
    private String type;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 目标类型 (post/comment/user等)
     */
    private String contentType;

    /**
     * 目标ID
     */
    private Long contentId;

    /**
     * 目标内容摘要
     */
    private String contentSnippet;

    /**
     * 目标缩略图
     */
    private String contentThumbnail;

    /**
     * 是否已读 (0=未读 1=已读)
     */
    private Integer isRead;

    /**
     * 已读时间
     */
    private java.util.Date readTime;

    /**
     * 删除标志 (0=正常 1=删除)
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
