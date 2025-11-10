package com.xypai.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 资料完整度VO
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletenessVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前完整度分数（0-100）
     */
    private Integer currentScore;

    /**
     * 完整度等级（优秀/良好/一般/较差/极差）
     */
    private String level;

    /**
     * 是否完整（≥80%）
     */
    private Boolean isComplete;

    /**
     * 核心字段得分（满分50）
     */
    private Integer coreFieldsScore;

    /**
     * 扩展字段得分（满分50）
     */
    private Integer extendedFieldsScore;

    /**
     * 完善建议列表
     */
    private List<String> suggestions;

    /**
     * 已完成项列表
     */
    private List<String> completedItems;

    /**
     * 距离完整还需多少分
     */
    private Integer remainingScore;

    /**
     * 完整度百分比（用于进度条显示）
     */
    private Integer percentage;

    /**
     * 进度条颜色（success/warning/danger）
     */
    private String progressColor;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 获取进度条颜色
     */
    public String getProgressColor() {
        if (currentScore >= 80) {
            return "success";  // 绿色
        } else if (currentScore >= 60) {
            return "warning";  // 橙色
        } else {
            return "danger";   // 红色
        }
    }

    /**
     * 获取提示消息
     */
    public String getMessage() {
        if (currentScore >= 90) {
            return "资料非常完整，你的主页将获得更多曝光！";
        } else if (currentScore >= 80) {
            return "资料已完整，继续保持！";
        } else if (currentScore >= 60) {
            return "资料一般，完善资料可提升排名";
        } else if (currentScore >= 40) {
            return "资料不完整，建议尽快完善";
        } else {
            return "资料严重不足，请立即完善基本信息";
        }
    }

    /**
     * 获取距离完整还需多少分
     */
    public Integer getRemainingScore() {
        if (currentScore >= 80) {
            return 0;
        }
        return 80 - currentScore;
    }

    /**
     * 获取百分比
     */
    public Integer getPercentage() {
        return currentScore;
    }
}

