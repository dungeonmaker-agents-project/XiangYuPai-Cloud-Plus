package org.dromara.appuser.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建APP用户DTO
 * Create App User DTO
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID (from sys_user)
     */
    private Long userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称 (可选，默认生成)
     */
    private String nickname;

    /**
     * 头像URL (可选，使用默认头像)
     */
    private String avatar;
}
