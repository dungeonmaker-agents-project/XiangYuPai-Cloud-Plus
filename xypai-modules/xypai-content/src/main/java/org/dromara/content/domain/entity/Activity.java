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
 * 组局活动表
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity")
public class Activity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Long activityId;

    /**
     * 发起人用户ID
     */
    private Long organizerId;

    /**
     * 活动类型编码
     */
    private String typeCode;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 封面图URL
     */
    private String coverImageUrl;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报名截止时间
     */
    private LocalDateTime registrationDeadline;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 最少人数
     */
    @Builder.Default
    private Integer minMembers = 2;

    /**
     * 最多人数
     */
    @Builder.Default
    private Integer maxMembers = 10;

    /**
     * 当前已报名人数(含发起人)
     */
    @Builder.Default
    private Integer currentMembers = 1;

    /**
     * 性别限制: all=不限, male=仅男, female=仅女
     */
    @Builder.Default
    private String genderLimit = "all";

    /**
     * 是否收费 0=免费 1=收费
     */
    @Builder.Default
    private Boolean isPaid = false;

    /**
     * 费用金额(元/人)
     */
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    /**
     * 费用说明
     */
    private String feeDescription;

    /**
     * 活动状态: recruiting=招募中, full=已满员, ongoing=进行中, completed=已结束, cancelled=已取消
     */
    @Builder.Default
    private String status = "recruiting";

    /**
     * 是否需要审核 0=自动通过 1=需审核
     */
    @Builder.Default
    private Boolean needApproval = false;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 浏览次数
     */
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 分享次数
     */
    @Builder.Default
    private Integer shareCount = 0;

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
