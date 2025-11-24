package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 限时专享用户分页查询结果
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitedTimePageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 用户列表
     */
    private List<LimitedTimeUserVo> list;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;
}
