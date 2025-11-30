package org.dromara.content.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户动态列表VO (用于RPC传输)
 *
 * <p>用途：批量查询多个用户的最新动态</p>
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户最新动态列表 (最多3条)
     */
    private List<FeedSimpleVo> feeds;

    /**
     * 动态总数
     */
    private Integer totalCount;
}
