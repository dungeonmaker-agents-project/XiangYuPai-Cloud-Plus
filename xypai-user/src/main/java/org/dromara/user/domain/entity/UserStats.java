package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户统计实体
 * 对应表: user_stats
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_stats")
public class UserStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID（主键）
     */
    @TableId(value = "stat_id", type = IdType.AUTO)
    private Long statId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关注数
     */
    @TableField("following_count")
    private Integer followingCount;

    /**
     * 粉丝数
     */
    @TableField("fans_count")
    private Integer fansCount;

    /**
     * 获赞总数
     */
    @TableField("likes_count")
    private Integer likesCount;

    /**
     * 动态数
     */
    @TableField("posts_count")
    private Integer postsCount;

    /**
     * 收藏数
     */
    @TableField("favorites_count")
    private Integer favoritesCount;

    /**
     * 动态数(moments)
     */
    @TableField("moments_count")
    private Integer momentsCount;

    /**
     * 收藏数(collections)
     */
    @TableField("collections_count")
    private Integer collectionsCount;

    /**
     * 技能数
     */
    @TableField("skills_count")
    private Integer skillsCount;

    /**
     * 订单数
     */
    @TableField("orders_count")
    private Integer ordersCount;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
