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
 * 话题表
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("topic")
public class Topic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 话题名称(唯一)
     */
    private String name;

    /**
     * 话题描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 参与人数
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer participantCount;

    /**
     * 帖子数
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer postCount;

    /**
     * 是否官方话题: 0=否,1=是
     */
    private Integer isOfficial;

    /**
     * 是否热门话题: 0=否,1=是
     */
    private Integer isHot;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
