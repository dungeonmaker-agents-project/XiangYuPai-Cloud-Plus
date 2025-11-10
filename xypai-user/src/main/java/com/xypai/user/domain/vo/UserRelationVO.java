package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关系VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRelationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 目标用户信息
     */
    private UserListVO targetUser;

    /**
     * 关系类型
     */
    private Integer type;

    /**
     * 关系类型描述
     */
    private String typeDesc;

    /**
     * 建立关系时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 是否为关注关系
     */
    private Boolean isFollow;

    /**
     * 是否为拉黑关系
     */
    private Boolean isBlock;
}
