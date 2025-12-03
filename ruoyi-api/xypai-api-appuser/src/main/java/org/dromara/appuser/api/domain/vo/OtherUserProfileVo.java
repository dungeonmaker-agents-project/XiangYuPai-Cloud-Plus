package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 对方主页聚合数据VO
 *
 * <p>对应UI文档中的 UserHeaderData + ActionBarData</p>
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUserProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 基本信息 ====================

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 封面图URL（主页背景）
     */
    private String coverUrl;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别: male, female, other
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个人简介
     */
    private String bio;

    // ==================== 等级与认证 ====================

    /**
     * 等级信息
     */
    private LevelInfo level;

    /**
     * 是否实名认证
     */
    private Boolean isRealVerified;

    /**
     * 是否大神认证
     */
    private Boolean isGodVerified;

    /**
     * 是否VIP
     */
    private Boolean isVip;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否可接单（有上架技能）
     */
    private Boolean isAvailable;

    // ==================== 位置与距离 ====================

    /**
     * 居住地
     */
    private String residence;

    /**
     * IP归属地
     */
    private String ipLocation;

    /**
     * 距离（米），null表示无法计算
     */
    private Integer distance;

    /**
     * 距离显示文本，如 "1.2km"
     */
    private String distanceDisplay;

    // ==================== 统计数据 ====================

    /**
     * 统计信息
     */
    private StatsInfo stats;

    // ==================== 关系状态 ====================

    /**
     * 关注状态: none-未关注, following-已关注, mutual-互相关注
     */
    private String followStatus;

    /**
     * 是否被当前用户拉黑
     */
    private Boolean isBlocked;

    // ==================== 操作栏信息 ====================

    /**
     * 是否可发消息
     */
    private Boolean canMessage;

    /**
     * 是否可解锁微信
     */
    private Boolean canUnlockWechat;

    /**
     * 是否已解锁微信
     */
    private Boolean wechatUnlocked;

    /**
     * 解锁价格（金币）
     */
    private BigDecimal unlockPrice;

    /**
     * 微信号（脱敏或完整，取决于是否解锁）
     */
    private String wechat;

    // ==================== 内嵌类 ====================

    /**
     * 等级信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 等级数值 (1-7)
         */
        private Integer value;

        /**
         * 等级名称 (青铜、白银、黄金、铂金、钻石、大师、王者)
         */
        private String name;

        /**
         * 等级图标URL
         */
        private String icon;

        /**
         * 等级颜色
         */
        private String color;
    }

    /**
     * 统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 关注数
         */
        private Integer followingCount;

        /**
         * 粉丝数
         */
        private Integer fansCount;

        /**
         * 获赞数
         */
        private Integer likesCount;

        /**
         * 动态数
         */
        private Integer momentsCount;

        /**
         * 技能数
         */
        private Integer skillsCount;
    }
}
