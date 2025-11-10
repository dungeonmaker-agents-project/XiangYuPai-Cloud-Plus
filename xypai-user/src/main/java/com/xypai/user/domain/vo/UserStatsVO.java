package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户统计VO
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 粉丝数量
     */
    private Integer followerCount;

    /**
     * 关注数量
     */
    private Integer followingCount;

    /**
     * 发布内容数量
     */
    private Integer contentCount;

    /**
     * 获赞总数
     */
    private Integer totalLikeCount;

    /**
     * 被收藏总数
     */
    private Integer totalCollectCount;

    /**
     * 发起组局总数
     */
    private Integer activityOrganizerCount;

    /**
     * 参与组局总数
     */
    private Integer activityParticipantCount;

    /**
     * 成功完成组局次数
     */
    private Integer activitySuccessCount;

    /**
     * 取消组局次数
     */
    private Integer activityCancelCount;

    /**
     * 组局信誉评分（5分制）
     */
    private BigDecimal activityOrganizerScore;

    /**
     * 组局成功率（百分比）
     */
    private BigDecimal activitySuccessRate;

    /**
     * 最后同步时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    /**
     * 是否为活跃用户
     */
    private Boolean isActive;

    /**
     * 是否为人气用户
     */
    private Boolean isPopular;

    /**
     * 是否为优质组局者
     */
    private Boolean isQualityOrganizer;

    /**
     * 粉丝关注比
     */
    private BigDecimal followerFollowingRatio;
}

