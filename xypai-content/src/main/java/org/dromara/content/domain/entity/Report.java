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
 * 举报实体
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("report")
public class Report implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 举报人用户ID
     */
    private Long userId;

    /**
     * 目标类型: feed=动态, comment=评论, user=用户
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 举报类型: harassment=骚扰, pornography=色情, fraud=诈骗, illegal=违法, spam=垃圾, other=其他
     */
    private String reasonType;

    /**
     * 举报描述(0-200字符)
     */
    private String description;

    /**
     * 举报图片(JSON数组,最多3张)
     */
    private String evidenceImages;

    /**
     * 审核状态: pending=待审核, processing=审核中, approved=已通过, rejected=已拒绝
     */
    private String status;

    /**
     * 审核结果说明
     */
    private String result;

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
