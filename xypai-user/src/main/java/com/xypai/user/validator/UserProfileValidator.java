package com.xypai.user.validator;

import org.dromara.common.core.exception.ServiceException;
import com.xypai.user.constant.UserConstants;
import com.xypai.user.domain.dto.UserProfileUpdateDTO;
import com.xypai.user.utils.UserUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户资料验证�?
 * 
 * 功能�?
 * 1. 字段格式验证
 * 2. 业务规则验证
 * 3. 敏感词过�?
 * 4. 数据合法性检�?
 *
 * @author Bob
 * @date 2025-01-14
 */
@Component
public class UserProfileValidator {

    /**
     * 验证用户资料更新DTO
     * 
     * @param updateDTO 更新DTO
     * @return 验证错误列表（空列表表示验证通过�?
     */
    public List<String> validate(UserProfileUpdateDTO updateDTO) {
        List<String> errors = new ArrayList<>();

        if (updateDTO == null) {
            errors.add("更新数据不能为空");
            return errors;
        }

        // 验证昵称
        if (updateDTO.getNickname() != null) {
            validateNickname(updateDTO.getNickname(), errors);
        }

        // 验证性别
        if (updateDTO.getGender() != null) {
            validateGender(updateDTO.getGender(), errors);
        }

        // 验证生日
        if (updateDTO.getBirthday() != null) {
            validateBirthday(updateDTO.getBirthday(), errors);
        }

        // 验证身高体重
        if (updateDTO.getHeight() != null) {
            validateHeight(updateDTO.getHeight(), errors);
        }
        if (updateDTO.getWeight() != null) {
            validateWeight(updateDTO.getWeight(), errors);
        }

        // 验证微信�?
        if (updateDTO.getWechat() != null) {
            validateWechat(updateDTO.getWechat(), errors);
        }

        // 验证个人简�?
        if (updateDTO.getBio() != null) {
            validateBio(updateDTO.getBio(), errors);
        }

        return errors;
    }

    /**
     * 验证并抛出异�?
     */
    public void validateAndThrow(UserProfileUpdateDTO updateDTO) {
        List<String> errors = validate(updateDTO);
        if (!errors.isEmpty()) {
            throw new ServiceException("资料验证失败：" + String.join("; ", errors));
        }
    }

    // ==================== 私有验证方法 ====================

    /**
     * 验证昵称
     */
    private void validateNickname(String nickname, List<String> errors) {
        if (nickname.trim().isEmpty()) {
            errors.add("昵称不能为空");
            return;
        }

        if (nickname.length() < UserConstants.NICKNAME_MIN_LENGTH 
            || nickname.length() > UserConstants.NICKNAME_MAX_LENGTH) {
            errors.add("昵称长度必须在2-20个字符之间");
        }

        if (!UserUtils.isValidNickname(nickname)) {
            errors.add("昵称包含敏感词或非法字符");
        }
    }

    /**
     * 验证性别
     */
    private void validateGender(Integer gender, List<String> errors) {
        if (gender < UserConstants.GENDER_UNSET || gender > UserConstants.GENDER_OTHER) {
            errors.add("性别值必须在0-3之间");
        }
    }

    /**
     * 验证生日
     */
    private void validateBirthday(LocalDate birthday, List<String> errors) {
        if (birthday.isAfter(LocalDate.now())) {
            errors.add("生日不能是未来日期");
        }

        LocalDate minBirthday = LocalDate.now().minusYears(100);
        if (birthday.isBefore(minBirthday)) {
            errors.add("生日不能早于100年前");
        }

        // 检查年龄（必须�?8岁）
        Integer age = UserUtils.calculateAge(birthday);
        if (age != null && age < 18) {
            errors.add("用户年龄必须满18岁");
        }
    }

    /**
     * 验证身高
     */
    private void validateHeight(Integer height, List<String> errors) {
        if (!UserUtils.isValidHeight(height)) {
            errors.add("身高必须在140-200cm之间");
        }
    }

    /**
     * 验证体重
     */
    private void validateWeight(Integer weight, List<String> errors) {
        if (!UserUtils.isValidWeight(weight)) {
            errors.add("体重必须在30-150kg之间");
        }
    }

    /**
     * 验证微信�?
     */
    private void validateWechat(String wechat, List<String> errors) {
        if (!UserUtils.isValidWechat(wechat)) {
            errors.add("微信号格式不正确（6-20位字母数字下划线中划线）");
        }
    }

    /**
     * 验证个人简�?
     */
    private void validateBio(String bio, List<String> errors) {
        if (bio.length() > UserConstants.BIO_MAX_LENGTH) {
            errors.add("个人简介长度不能超过500个字符");
        }

        if (!UserUtils.isValidBio(bio)) {
            errors.add("个人简介包含敏感词或非法内容");
        }
    }

    /**
     * 验证在线状�?
     */
    public static boolean isValidOnlineStatus(Integer status) {
        return status != null 
            && status >= UserConstants.ONLINE_STATUS_OFFLINE 
            && status <= UserConstants.ONLINE_STATUS_INVISIBLE;
    }

    /**
     * 验证微信解锁条件
     */
    public static boolean isValidWechatUnlockCondition(Integer condition) {
        return condition != null 
            && condition >= UserConstants.WECHAT_UNLOCK_PUBLIC 
            && condition <= UserConstants.WECHAT_UNLOCK_PRIVATE;
    }

    /**
     * 验证用户状�?
     */
    public static boolean isValidUserStatus(Integer status) {
        return status != null 
            && status >= UserConstants.USER_STATUS_DISABLED 
            && status <= UserConstants.USER_STATUS_PENDING;
    }
}

