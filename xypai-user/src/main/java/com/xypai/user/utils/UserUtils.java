package com.xypai.user.utils;

import com.xypai.user.domain.entity.User;
import com.xypai.user.domain.entity.UserProfileNew;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * 用户工具类
 *
 * @author Bob
 * @date 2025-01-14
 */
public class UserUtils {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern WECHAT_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{6,20}$");

    /**
     * 验证手机号格式
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile).matches();
    }

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证微信号格式
     */
    public static boolean isValidWechat(String wechat) {
        if (wechat == null || wechat.isEmpty()) {
            return false;
        }
        return WECHAT_PATTERN.matcher(wechat).matches();
    }

    /**
     * 手机号脱敏
     * 
     * @param mobile 原手机号
     * @return 脱敏后的手机号（例如：138****5678）
     */
    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 11) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 邮箱脱敏
     * 
     * @param email 原邮箱
     * @return 脱敏后的邮箱（例如：zh***@example.com）
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "***@" + domain;
        }
        
        return username.substring(0, 2) + "***@" + domain;
    }

    /**
     * 真实姓名脱敏
     * 
     * @param realName 原真实姓名
     * @return 脱敏后的姓名（例如：张*、欧阳**）
     */
    public static String maskRealName(String realName) {
        if (realName == null || realName.isEmpty()) {
            return realName;
        }
        
        int length = realName.length();
        if (length == 1) {
            return realName;
        } else if (length == 2) {
            return realName.charAt(0) + "*";
        } else {
            // 保留第一个字，其他用*替代
            StringBuilder masked = new StringBuilder();
            masked.append(realName.charAt(0));
            for (int i = 1; i < length; i++) {
                masked.append("*");
            }
            return masked.toString();
        }
    }

    /**
     * 身份证号脱敏
     * 
     * @param idCard 原身份证号
     * @return 脱敏后的身份证号（例如：110***********1234）
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(14);
    }

    /**
     * 根据生日计算年龄
     * 
     * @param birthday 生日
     * @return 年龄
     */
    public static Integer calculateAge(LocalDate birthday) {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    /**
     * 检查VIP是否有效
     * 
     * @param isVip 是否VIP
     * @param vipExpireTime VIP过期时间
     * @return 是否有效
     */
    public static boolean isVipValid(Boolean isVip, LocalDateTime vipExpireTime) {
        if (!Boolean.TRUE.equals(isVip)) {
            return false;
        }
        if (vipExpireTime == null) {
            return true; // 永久VIP
        }
        return vipExpireTime.isAfter(LocalDateTime.now());
    }

    /**
     * 检查账户是否锁定
     * 
     * @param loginLockedUntil 锁定截止时间
     * @return 是否锁定
     */
    public static boolean isAccountLocked(LocalDateTime loginLockedUntil) {
        return loginLockedUntil != null && loginLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 检查密码是否过期（90天）
     * 
     * @param passwordUpdatedAt 密码更新时间
     * @return 是否过期
     */
    public static boolean isPasswordExpired(LocalDateTime passwordUpdatedAt) {
        if (passwordUpdatedAt == null) {
            return true; // 未设置更新时间，视为过期
        }
        return passwordUpdatedAt.plusDays(90).isBefore(LocalDateTime.now());
    }

    /**
     * 生成默认昵称（基于手机号）
     * 
     * @param mobile 手机号
     * @return 默认昵称（例如：用户138****5678）
     */
    public static String generateDefaultNickname(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return "用户" + System.currentTimeMillis();
        }
        return "用户" + maskMobile(mobile);
    }

    /**
     * 检查昵称是否合法（不含敏感词）
     * 
     * @param nickname 昵称
     * @return 是否合法
     */
    public static boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }
        
        // 长度检查
        if (nickname.length() < 1 || nickname.length() > 20) {
            return false;
        }
        
        // TODO: 敏感词检查（可接入敏感词库）
        String[] sensitiveWords = {"管理员", "admin", "系统", "官方"};
        for (String word : sensitiveWords) {
            if (nickname.contains(word)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 检查个人简介是否合法
     * 
     * @param bio 个人简介
     * @return 是否合法
     */
    public static boolean isValidBio(String bio) {
        if (bio == null) {
            return true; // 允许为空
        }
        
        // 长度检查
        if (bio.length() > 500) {
            return false;
        }
        
        // TODO: 敏感词检查
        
        return true;
    }

    /**
     * 格式化粉丝数展示
     * 
     * @param count 粉丝数
     * @return 格式化后的字符串（例如：1.2万、10.5万）
     */
    public static String formatFollowerCount(Integer count) {
        if (count == null || count == 0) {
            return "0";
        }
        
        if (count < 10000) {
            return count.toString();
        } else if (count < 100000) {
            return String.format("%.1f万", count / 10000.0);
        } else if (count < 10000000) {
            return String.format("%.0f万", count / 10000.0);
        } else {
            return String.format("%.1f千万", count / 10000000.0);
        }
    }

    /**
     * 格式化点赞数展示
     * 
     * @param count 点赞数
     * @return 格式化后的字符串
     */
    public static String formatLikeCount(Integer count) {
        return formatFollowerCount(count);
    }

    /**
     * 检查身高是否在合理范围
     * 
     * @param height 身高（cm）
     * @return 是否合理
     */
    public static boolean isValidHeight(Integer height) {
        return height != null && height >= 140 && height <= 200;
    }

    /**
     * 检查体重是否在合理范围
     * 
     * @param weight 体重（kg）
     * @return 是否合理
     */
    public static boolean isValidWeight(Integer weight) {
        return weight != null && weight >= 30 && weight <= 150;
    }

    /**
     * 检查年龄是否在合理范围（18-100岁）
     * 
     * @param age 年龄
     * @return 是否合理
     */
    public static boolean isValidAge(Integer age) {
        return age != null && age >= 18 && age <= 100;
    }

    /**
     * 计算BMI（身体质量指数）
     * 
     * @param height 身高（cm）
     * @param weight 体重（kg）
     * @return BMI值
     */
    public static Double calculateBMI(Integer height, Integer weight) {
        if (height == null || weight == null || height == 0) {
            return null;
        }
        
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    /**
     * 获取BMI评级
     * 
     * @param bmi BMI值
     * @return 评级（偏瘦/正常/偏胖/肥胖）
     */
    public static String getBMILevel(Double bmi) {
        if (bmi == null) {
            return "未知";
        }
        
        if (bmi < 18.5) {
            return "偏瘦";
        } else if (bmi < 24) {
            return "正常";
        } else if (bmi < 28) {
            return "偏胖";
        } else {
            return "肥胖";
        }
    }

    /**
     * 判断用户是否在线（5分钟内活跃）
     * 
     * @param lastOnlineTime 最后在线时间
     * @return 是否在线
     */
    public static boolean isUserOnline(LocalDateTime lastOnlineTime) {
        if (lastOnlineTime == null) {
            return false;
        }
        return lastOnlineTime.plusMinutes(5).isAfter(LocalDateTime.now());
    }

    /**
     * 获取用户年龄段
     * 
     * @param age 年龄
     * @return 年龄段（18-24/25-30/31-40/41+）
     */
    public static String getAgeRange(Integer age) {
        if (age == null) {
            return "未知";
        }
        
        if (age < 18) {
            return "未成年";
        } else if (age <= 24) {
            return "18-24岁";
        } else if (age <= 30) {
            return "25-30岁";
        } else if (age <= 40) {
            return "31-40岁";
        } else {
            return "41岁以上";
        }
    }
}

