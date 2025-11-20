package org.dromara.xypai.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * App登录响应VO
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppLoginVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    @JsonProperty("token")
    private String accessToken;

    /**
     * 令牌过期时间（秒）
     */
    @JsonProperty("expireIn")
    private Long expireIn;

    /**
     * 用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 用户昵称
     */
    @JsonProperty("nickname")
    private String nickname;

    /**
     * 用户头像
     */
    @JsonProperty("avatar")
    private String avatar;

    /**
     * 是否新用户（需要完善资料）
     */
    @JsonProperty("isNewUser")
    private Boolean isNewUser;
}
