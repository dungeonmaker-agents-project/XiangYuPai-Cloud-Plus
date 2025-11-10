package com.xypai.content.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容统计实体
 * 
 * 设计理念：
 * 1. 统计数据与业务数据分离，避免高并发锁竞争
 * 2. Redis主存储 + MySQL持久化，每5分钟批量同步
 * 3. 读取优先Redis，不存在时回源MySQL
 * 4. 定时任务修正脏数据，保证最终一致性
 *
 * @author Charlie (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("content_stats")
public class ContentStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID（主键）
     */
    @TableId("content_id")
    private Long contentId;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 点赞数量
     */
    @TableField("like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 评论数量
     */
    @TableField("comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    /**
     * 分享数量
     */
    @TableField("share_count")
    @Builder.Default
    private Integer shareCount = 0;

    /**
     * 收藏数量
     */
    @TableField("collect_count")
    @Builder.Default
    private Integer collectCount = 0;

    /**
     * 最后同步时间（从Redis同步）
     */
    @TableField("last_sync_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 获取总互动数
     */
    public Integer getTotalInteraction() {
        return likeCount + commentCount + shareCount + collectCount;
    }

    /**
     * 获取热度分数（简化版本）
     * 可根据实际需求调整权重
     */
    public Double getHeatScore() {
        return viewCount * 0.1 + 
               likeCount * 1.0 + 
               commentCount * 2.0 + 
               shareCount * 3.0 + 
               collectCount * 1.5;
    }

    /**
     * 是否为热门内容
     * 可根据实际阈值调整
     */
    public boolean isHot() {
        return getHeatScore() > 100.0;
    }
}

