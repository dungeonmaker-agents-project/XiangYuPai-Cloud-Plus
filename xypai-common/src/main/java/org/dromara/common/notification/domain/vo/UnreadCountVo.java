package org.dromara.common.notification.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读数统计视图对象
 * Unread Count VO
 *
 * @author XiangYuPai Team
 */
@Data
public class UnreadCountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 点赞未读数
     */
    private Long likeCount = 0L;

    /**
     * 评论未读数
     */
    private Long commentCount = 0L;

    /**
     * 关注未读数
     */
    private Long followCount = 0L;

    /**
     * 系统通知未读数
     */
    private Long systemCount = 0L;

    /**
     * 活动通知未读数
     */
    private Long activityCount = 0L;

    /**
     * 总未读数
     */
    private Long totalCount = 0L;
}
