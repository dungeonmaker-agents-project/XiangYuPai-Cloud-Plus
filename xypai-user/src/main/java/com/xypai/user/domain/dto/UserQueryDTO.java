package com.xypai.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询DTO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名（模糊查询）
     */
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    /**
     * 手机号（模糊查询）
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String mobile;

    /**
     * 昵称（模糊查询）
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 结束时间
     */
    private String endTime;
}
