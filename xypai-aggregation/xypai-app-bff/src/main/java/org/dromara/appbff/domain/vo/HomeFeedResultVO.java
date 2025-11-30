package org.dromara.appbff.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 首页用户推荐 Feed 响应 VO
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeFeedResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户列表
     */
    private List<UserCardVO> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 是否有更多
     */
    private Boolean hasMore;
}
