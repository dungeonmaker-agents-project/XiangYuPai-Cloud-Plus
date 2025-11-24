package org.dromara.chat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读消息数VO
 * Unread Count Response
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 赞和收藏未读数
     */
    @Builder.Default
    private Integer likes = 0;

    /**
     * 评论未读数
     */
    @Builder.Default
    private Integer comments = 0;

    /**
     * 粉丝未读数
     */
    @Builder.Default
    private Integer followers = 0;

    /**
     * 系统通知未读数
     */
    @Builder.Default
    private Integer system = 0;

    /**
     * 总未读数
     */
    @Builder.Default
    private Integer total = 0;
}
