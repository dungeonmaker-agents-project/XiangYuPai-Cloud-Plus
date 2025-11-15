package org.dromara.common.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 通知实体
 * Notification Entity
 *
 * @author XiangYuPai Team
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
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 发送用户ID (系统通知为null)
     */
    private Long fromUserId;

    /**
     * 通知类型: like/comment/follow/system/activity
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务类型: post/moment/comment等
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 扩展数据 (JSON格式)
     */
    private String extraData;

    /**
     * 是否已读: 0=未读, 1=已读
     */
    private Integer isRead;

    /**
     * 已读时间
     */
    private Date readAt;

    /**
     * 状态: 0=正常, 1=已删除
     */
    private Integer status;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(value = "deleted")
    private Long deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(value = "version")
    private Long version;
}
