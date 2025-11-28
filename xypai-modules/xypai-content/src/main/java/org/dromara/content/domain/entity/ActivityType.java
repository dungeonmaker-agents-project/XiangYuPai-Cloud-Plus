package org.dromara.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动类型配置表
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_type")
public class ActivityType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 类型ID
     */
    @TableId(value = "type_id", type = IdType.AUTO)
    private Long typeId;

    /**
     * 类型编码 (billiards, board_game, etc.)
     */
    private String typeCode;

    /**
     * 类型名称 (台球, 桌游, etc.)
     */
    private String typeName;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 排序顺序
     */
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 是否热门 0=否 1=是
     */
    @Builder.Default
    private Boolean isHot = false;

    /**
     * 状态 0=禁用 1=启用
     */
    @Builder.Default
    private Boolean status = true;

    /**
     * 创建时间
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 0=未删除 1=已删除
     */
    @TableLogic
    private Integer deleted;
}
