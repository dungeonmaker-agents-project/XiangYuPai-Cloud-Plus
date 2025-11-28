package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动列表项VO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动类型编码
     */
    private String typeCode;

    /**
     * 活动类型名称
     */
    private String typeName;

    /**
     * 活动标题
     */
    private String title;

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
     * 时间显示文本
     */
    private String timeDisplay;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 距离（米）
     */
    private Integer distance;

    /**
     * 距离显示文本
     */
    private String distanceDisplay;

    /**
     * 当前人数
     */
    private Integer currentMembers;

    /**
     * 最大人数
     */
    private Integer maxMembers;

    /**
     * 人数显示文本
     */
    private String membersDisplay;

    /**
     * 性别限制
     */
    private String genderLimit;

    /**
     * 性别限制显示文本
     */
    private String genderLimitDisplay;

    /**
     * 是否收费
     */
    private Boolean isPaid;

    /**
     * 费用金额
     */
    private BigDecimal fee;

    /**
     * 费用显示文本
     */
    private String feeDisplay;

    /**
     * 活动状态
     */
    private String status;

    /**
     * 状态显示文本
     */
    private String statusDisplay;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 发起人ID
     */
    private Long organizerId;

    /**
     * 发起人昵称
     */
    private String organizerNickname;

    /**
     * 发起人头像
     */
    private String organizerAvatarUrl;

    /**
     * 发起人是否认证
     */
    private Boolean organizerIsVerified;

    /**
     * 参与者头像列表（最多5个）
     */
    private List<String> participantAvatars;
}
