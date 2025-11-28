package org.dromara.content.api.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布活动DTO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityPublishDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 图片URL列表
     */
    private List<String> imageUrls;

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
    private Integer minMembers;

    /**
     * 最大人数
     */
    private Integer maxMembers;

    /**
     * 性别限制: all, male, female
     */
    private String genderLimit;

    /**
     * 是否收费
     */
    private Boolean isPaid;

    /**
     * 费用金额
     */
    private BigDecimal fee;

    /**
     * 费用说明
     */
    private String feeDescription;

    /**
     * 是否需要审核报名
     */
    private Boolean needApproval;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 标签列表
     */
    private List<String> tags;
}
