package org.dromara.common.notification.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.notification.domain.entity.Notification;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 通知视图对象
 * Notification VO
 *
 * @author XiangYuPai Team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Notification.class)
public class NotificationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @ExcelProperty(value = "通知ID")
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 发送用户ID
     */
    private Long fromUserId;

    /**
     * 发送用户昵称 (关联查询填充)
     */
    private String fromUserNickname;

    /**
     * 发送用户头像 (关联查询填充)
     */
    private String fromUserAvatar;

    /**
     * 通知类型
     */
    @ExcelProperty(value = "通知类型")
    private String type;

    /**
     * 通知标题
     */
    @ExcelProperty(value = "标题")
    private String title;

    /**
     * 通知内容
     */
    @ExcelProperty(value = "内容")
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

    /**
     * 是否已读
     */
    @ExcelProperty(value = "是否已读")
    private Integer isRead;

    /**
     * 已读时间
     */
    private Date readAt;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;
}
