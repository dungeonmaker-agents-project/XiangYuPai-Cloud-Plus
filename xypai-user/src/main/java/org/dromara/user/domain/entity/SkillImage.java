package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能展示图片实体
 * 对应表: skill_images
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_images")
public class SkillImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片ID（主键）
     */
    @TableId(value = "image_id", type = IdType.AUTO)
    private Long imageId;

    /**
     * 技能ID
     */
    @TableField("skill_id")
    private Long skillId;

    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 图片排序（1-5）
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
