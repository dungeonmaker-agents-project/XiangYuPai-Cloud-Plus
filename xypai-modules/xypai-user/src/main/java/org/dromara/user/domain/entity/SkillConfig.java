package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能配置实体
 * 对应表: skill_config
 *
 * 用于添加技能页面的技能选择器网格
 * 对应UI文档: 添加技能页_结构文档.md
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_config")
public class SkillConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID（主键）
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 技能名称（王者荣耀、探店等）
     */
    @TableField("name")
    private String name;

    /**
     * 技能图标URL
     */
    @TableField("icon")
    private String icon;

    /**
     * 技能类型: online=线上, offline=线下
     */
    @TableField("skill_type")
    private String skillType;

    /**
     * 分类（游戏、生活服务等）
     */
    @TableField("category")
    private String category;

    /**
     * 排序序号（越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态: 0=禁用, 1=启用
     */
    @TableField("status")
    private Integer status;

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
