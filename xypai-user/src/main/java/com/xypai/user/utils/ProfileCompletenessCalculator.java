package com.xypai.user.utils;

import com.xypai.user.domain.entity.UserOccupation;
import com.xypai.user.domain.entity.UserProfileNew;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 用户资料完整度计算器
 * 
 * 评分标准（满分100分）：
 * - 核心字段（50分）：昵称、头像、性别、生日、城市
 * - 扩展字段（50分）：简介、身高、体重、职业、微信、背景图、实名认证等
 *
 * @author Bob
 * @date 2025-01-14
 */
public class ProfileCompletenessCalculator {

    /**
     * 计算资料完整度
     * 
     * @param profile 用户资料
     * @param occupations 职业标签列表
     * @return 完整度分数（0-100）
     */
    public static int calculate(UserProfileNew profile, List<UserOccupation> occupations) {
        if (profile == null) {
            return 0;
        }

        int score = 0;

        // ==================== 核心字段（50分） ====================
        
        // 昵称（10分）
        if (isNotEmpty(profile.getNickname())) {
            score += 10;
        }

        // 头像（10分）
        if (isNotEmpty(profile.getAvatar())) {
            score += 10;
        }

        // 性别（10分）
        if (profile.getGender() != null && profile.getGender() > 0) {
            score += 10;
        }

        // 生日（10分）
        if (profile.getBirthday() != null) {
            score += 10;
        }

        // 城市（10分）
        if (profile.getCityId() != null) {
            score += 10;
        }

        // ==================== 扩展字段（50分） ====================

        // 个人简介（5分）
        if (isNotEmpty(profile.getBio())) {
            score += 5;
        }

        // 身高（5分）
        if (profile.getHeight() != null) {
            score += 5;
        }

        // 体重（5分）
        if (profile.getWeight() != null) {
            score += 5;
        }

        // 职业标签（10分）- 至少1个
        if (!CollectionUtils.isEmpty(occupations)) {
            score += 10;
        }

        // 微信号（5分）
        if (isNotEmpty(profile.getWechat())) {
            score += 5;
        }

        // 背景图（5分）
        if (isNotEmpty(profile.getBackgroundImage())) {
            score += 5;
        }

        // 实名认证（15分）- 最高分项
        if (Boolean.TRUE.equals(profile.getIsRealVerified())) {
            score += 15;
        }

        return Math.min(score, 100); // 确保不超过100分
    }

    /**
     * 判断字符串是否非空
     */
    private static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 获取完整度等级
     * 
     * @param score 完整度分数
     * @return 等级描述
     */
    public static String getCompletenessLevel(int score) {
        if (score >= 90) {
            return "优秀";
        } else if (score >= 80) {
            return "良好";
        } else if (score >= 60) {
            return "一般";
        } else if (score >= 40) {
            return "较差";
        } else {
            return "极差";
        }
    }

    /**
     * 检查资料是否完整（≥80分）
     * 
     * @param score 完整度分数
     * @return 是否完整
     */
    public static boolean isComplete(int score) {
        return score >= 80;
    }

    /**
     * 获取需要完善的建议
     * 
     * @param profile 用户资料
     * @param occupations 职业标签
     * @return 建议列表
     */
    public static List<String> getSuggestions(UserProfileNew profile, List<UserOccupation> occupations) {
        List<String> suggestions = new java.util.ArrayList<>();

        if (profile == null) {
            suggestions.add("请完善基本资料");
            return suggestions;
        }

        // 核心字段建议
        if (isNotEmpty(profile.getNickname()) == false) {
            suggestions.add("设置昵称");
        }
        if (isNotEmpty(profile.getAvatar()) == false) {
            suggestions.add("上传头像");
        }
        if (profile.getGender() == null || profile.getGender() == 0) {
            suggestions.add("选择性别");
        }
        if (profile.getBirthday() == null) {
            suggestions.add("填写生日");
        }
        if (profile.getCityId() == null) {
            suggestions.add("选择城市");
        }

        // 扩展字段建议
        if (isNotEmpty(profile.getBio()) == false) {
            suggestions.add("填写个人简介");
        }
        if (profile.getHeight() == null || profile.getWeight() == null) {
            suggestions.add("完善身高体重");
        }
        if (CollectionUtils.isEmpty(occupations)) {
            suggestions.add("添加职业标签");
        }
        if (Boolean.FALSE.equals(profile.getIsRealVerified())) {
            suggestions.add("完成实名认证（+15分）");
        }

        return suggestions;
    }
}

