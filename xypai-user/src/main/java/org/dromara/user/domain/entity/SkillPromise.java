package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能服务承诺实体
 * 对应表: skill_promises
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_promises")
public class SkillPromise implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 承诺ID（主键）
     */
    @TableId(value = "promise_id", type = IdType.AUTO)
    private Long promiseId;

    /**
     * 技能ID
     */
    @TableField("skill_id")
    private Long skillId;

    /**
     * 承诺内容（例如：24小时响应、不满意退款）
     */
    @TableField("promise_text")
    private String promiseText;

    /**
     * 承诺排序
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
