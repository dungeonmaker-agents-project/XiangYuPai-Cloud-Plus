package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 段位配置实体
 * 对应表: skill_rank_config
 *
 * 用于添加技能页面的段位选择器
 * 对应UI文档: 添加技能页_结构文档.md - RankPickerModal
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_rank_config")
public class SkillRankConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 段位ID（主键）
     */
    @TableId(value = "rank_id", type = IdType.AUTO)
    private Long rankId;

    /**
     * 技能配置ID（关联skill_config）
     */
    @TableField("skill_config_id")
    private Long skillConfigId;

    /**
     * 服务区: qq=QQ区, weixin=微信区, default=通用
     */
    @TableField("server")
    private String server;

    /**
     * 段位名称（永恒钻石、至尊星耀等）
     */
    @TableField("rank_name")
    private String rankName;

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
}
