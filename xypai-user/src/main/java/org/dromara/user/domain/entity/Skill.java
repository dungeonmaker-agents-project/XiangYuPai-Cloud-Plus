package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能实体
 * 对应表: skills
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skills")
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能ID（主键）
     */
    @TableId(value = "skill_id", type = IdType.AUTO)
    private Long skillId;

    /**
     * 用户ID（技能拥有者）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 技能名称（2-50字符）
     */
    @TableField("skill_name")
    private String skillName;

    /**
     * 技能类型: online, offline
     */
    @TableField("skill_type")
    private String skillType;

    /**
     * 封面图URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 技能介绍（10-500字符）
     */
    @TableField("description")
    private String description;

    /**
     * 价格（元）
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 价格单位: 局, 小时
     */
    @TableField("price_unit")
    private String priceUnit;

    /**
     * 是否上架（0-下架，1-上架）
     */
    @TableField("is_online")
    private Boolean isOnline;

    /**
     * 评分（0-5.00）
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 评价数量
     */
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 订单数量
     */
    @TableField("order_count")
    private Integer orderCount;

    // === Online Skill Specific Fields ===

    /**
     * 游戏名称（线上技能专用）
     */
    @TableField("game_name")
    private String gameName;

    /**
     * 游戏段位（线上技能专用）
     */
    @TableField("game_rank")
    private String gameRank;

    /**
     * 服务时长（小时/局，线上技能专用）
     */
    @TableField("service_hours")
    private BigDecimal serviceHours;

    // === Offline Skill Specific Fields ===

    /**
     * 服务类型（线下技能专用）
     */
    @TableField("service_type")
    private String serviceType;

    /**
     * 服务地点（线下技能专用）
     */
    @TableField("service_location")
    private String serviceLocation;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
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
