package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 用户统计表实体（独立存储，配合Redis使用）
 * 
 * 设计理念：
 * 1. 读取：优先读Redis，Redis不存在时回源MySQL
 * 2. 写入：通过消息队列异步更新MySQL
 * 3. 一致性：定时任务每10分钟修正脏数据
 * 4. 性能：避免UserProfile表成为热点，解决锁竞争
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_stats")
public class UserStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键）
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 粉丝数量
     */
    @TableField("follower_count")
    @Builder.Default
    private Integer followerCount = 0;

    /**
     * 关注数量
     */
    @TableField("following_count")
    @Builder.Default
    private Integer followingCount = 0;

    /**
     * 发布内容数量
     */
    @TableField("content_count")
    @Builder.Default
    private Integer contentCount = 0;

    /**
     * 获赞总数
     */
    @TableField("total_like_count")
    @Builder.Default
    private Integer totalLikeCount = 0;

    /**
     * 被收藏总数
     */
    @TableField("total_collect_count")
    @Builder.Default
    private Integer totalCollectCount = 0;

    /**
     * 发起组局总数
     */
    @TableField("activity_organizer_count")
    @Builder.Default
    private Integer activityOrganizerCount = 0;

    /**
     * 参与组局总数
     */
    @TableField("activity_participant_count")
    @Builder.Default
    private Integer activityParticipantCount = 0;

    /**
     * 成功完成组局次数
     */
    @TableField("activity_success_count")
    @Builder.Default
    private Integer activitySuccessCount = 0;

    /**
     * 取消组局次数
     */
    @TableField("activity_cancel_count")
    @Builder.Default
    private Integer activityCancelCount = 0;

    /**
     * 组局信誉评分（5分制）
     */
    @TableField("activity_organizer_score")
    @Builder.Default
    private BigDecimal activityOrganizerScore = BigDecimal.ZERO;

    /**
     * 组局成功率（百分比）
     */
    @TableField("activity_success_rate")
    @Builder.Default
    private BigDecimal activitySuccessRate = BigDecimal.ZERO;

    /**
     * 最后同步时间（用于监控同步延迟）
     */
    @TableField("last_sync_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ==================== 业务方法 ====================

    /**
     * 增加粉丝数
     */
    public void incrementFollowerCount() {
        this.followerCount++;
    }

    /**
     * 减少粉丝数
     */
    public void decrementFollowerCount() {
        if (this.followerCount > 0) {
            this.followerCount--;
        }
    }

    /**
     * 增加关注数
     */
    public void incrementFollowingCount() {
        this.followingCount++;
    }

    /**
     * 减少关注数
     */
    public void decrementFollowingCount() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }

    /**
     * 增加内容数
     */
    public void incrementContentCount() {
        this.contentCount++;
    }

    /**
     * 减少内容数
     */
    public void decrementContentCount() {
        if (this.contentCount > 0) {
            this.contentCount--;
        }
    }

    /**
     * 增加点赞数
     */
    public void incrementLikeCount(int count) {
        this.totalLikeCount += count;
    }

    /**
     * 增加收藏数
     */
    public void incrementCollectCount(int count) {
        this.totalCollectCount += count;
    }

    /**
     * 增加组局次数
     */
    public void incrementActivityOrganizerCount() {
        this.activityOrganizerCount++;
    }

    /**
     * 增加参与组局次数
     */
    public void incrementActivityParticipantCount() {
        this.activityParticipantCount++;
    }

    /**
     * 标记组局成功
     */
    public void markActivitySuccess() {
        this.activitySuccessCount++;
        recalculateSuccessRate();
    }

    /**
     * 标记组局取消
     */
    public void markActivityCancel() {
        this.activityCancelCount++;
        recalculateSuccessRate();
    }

    /**
     * 重新计算组局成功率
     */
    public void recalculateSuccessRate() {
        int totalActivities = this.activityOrganizerCount;
        if (totalActivities == 0) {
            this.activitySuccessRate = BigDecimal.ZERO;
            return;
        }
        
        BigDecimal successCount = new BigDecimal(this.activitySuccessCount);
        BigDecimal total = new BigDecimal(totalActivities);
        this.activitySuccessRate = successCount
            .divide(total, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }

    /**
     * 更新组局评分
     */
    public void updateOrganizerScore(BigDecimal newScore) {
        if (newScore != null && newScore.compareTo(BigDecimal.ZERO) >= 0 
            && newScore.compareTo(new BigDecimal("5")) <= 0) {
            this.activityOrganizerScore = newScore;
        }
    }

    /**
     * 检查是否为活跃用户（发布内容>10）
     */
    public boolean isActiveUser() {
        return this.contentCount != null && this.contentCount > 10;
    }

    /**
     * 检查是否为人气用户（粉丝>1000）
     */
    public boolean isPopularUser() {
        return this.followerCount != null && this.followerCount > 1000;
    }

    /**
     * 检查是否为优质组局者（评分>4.5，成功率>80%）
     */
    public boolean isQualityOrganizer() {
        return this.activityOrganizerScore != null 
            && this.activityOrganizerScore.compareTo(new BigDecimal("4.5")) > 0
            && this.activitySuccessRate != null
            && this.activitySuccessRate.compareTo(new BigDecimal("80")) > 0;
    }

    /**
     * 获取粉丝关注比（粉丝数/关注数）
     */
    public BigDecimal getFollowerFollowingRatio() {
        if (followingCount == null || followingCount == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(followerCount)
            .divide(new BigDecimal(followingCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 更新最后同步时间
     */
    public void updateSyncTime() {
        this.lastSyncTime = LocalDateTime.now();
    }

    /**
     * 检查是否需要同步（超过5分钟未同步）
     */
    public boolean needSync() {
        if (lastSyncTime == null) {
            return true;
        }
        return lastSyncTime.plusMinutes(5).isBefore(LocalDateTime.now());
    }
}

