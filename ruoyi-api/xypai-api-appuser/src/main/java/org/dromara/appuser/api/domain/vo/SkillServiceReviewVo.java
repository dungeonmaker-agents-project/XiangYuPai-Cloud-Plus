package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能服务评价 VO (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillServiceReviewVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 评分 (1-5)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 评价者信息
     */
    private ReviewerVo reviewer;

    /**
     * 服务提供者回复
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyAt;

    /**
     * 评价者信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewerVo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像URL
         */
        private String avatar;
    }
}
