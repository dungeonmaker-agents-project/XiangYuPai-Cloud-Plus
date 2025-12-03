package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 对方主页VO
 * 对应UI文档中的 UserHeaderData + ActionBarData
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对方主页数据")
public class OtherUserProfileVO {

    // ==================== 基本信息 ====================

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "性别: male, female, other")
    private String gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "个人简介")
    private String bio;

    // ==================== 等级与认证 ====================

    @Schema(description = "等级信息")
    private LevelVO level;

    @Schema(description = "是否实名认证")
    private Boolean isVerified;

    @Schema(description = "是否大神认证")
    private Boolean isExpert;

    @Schema(description = "是否VIP")
    private Boolean isVip;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "是否可接单")
    private Boolean isAvailable;

    // ==================== 位置与距离 ====================

    @Schema(description = "距离显示文本")
    private String distance;

    // ==================== 统计数据 ====================

    @Schema(description = "粉丝数")
    private Integer followerCount;

    @Schema(description = "关注数")
    private Integer followingCount;

    @Schema(description = "获赞数")
    private Integer likesCount;

    // ==================== 关系状态 ====================

    @Schema(description = "是否已关注")
    private Boolean isFollowed;

    @Schema(description = "关注状态: none, following, mutual")
    private String followStatus;

    // ==================== 操作栏信息 ====================

    @Schema(description = "是否可发消息")
    private Boolean canMessage;

    @Schema(description = "是否可解锁微信")
    private Boolean canUnlockWechat;

    @Schema(description = "是否已解锁微信")
    private Boolean wechatUnlocked;

    @Schema(description = "解锁价格")
    private BigDecimal unlockPrice;

    // ==================== 内嵌类 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "等级信息")
    public static class LevelVO {
        @Schema(description = "等级数值")
        private Integer value;

        @Schema(description = "等级名称")
        private String name;

        @Schema(description = "等级图标URL")
        private String icon;
    }
}
