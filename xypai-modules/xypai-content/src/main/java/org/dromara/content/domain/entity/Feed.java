package org.dromara.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 动态表
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("feed")
public class Feed implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 动态类型: 1=动态,2=活动,3=技能
     */
    private Integer type;

    /**
     * 标题(0-50字符)
     */
    private String title;

    /**
     * 内容(1-1000字符)
     */
    private String content;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 城市ID
     */
    private Long cityId;

    /**
     * 点赞数
     */
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    private Integer likeCount = 0;

    /**
     * 评论数
     */
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    private Integer commentCount = 0;

    /**
     * 分享数
     */
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    private Integer shareCount = 0;

    /**
     * 收藏数
     */
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    private Integer collectCount = 0;

    /**
     * 浏览数
     */
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    private Integer viewCount = 0;

    /**
     * 可见范围: 0=公开,1=仅好友,2=仅自己
     */
    private Integer visibility;

    /**
     * 状态: 0=正常,1=审核中,2=已下架
     */
    private Integer status;

    /**
     * 删除标记: 0=未删除,1=已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间（数据库自动填充）
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    /**
     * 更新时间（数据库自动填充）
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;

    /**
     * 创建时间戳(用于排序)
     */
    private Long createdTimestamp;

    /**
     * 乐观锁（数据库默认为0）
     */
    @Version
    @TableField(insertStrategy = FieldStrategy.NEVER)
    private Integer version;

}
