package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 筛选用户分页查询结果 (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterUserPageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 用户列表
     */
    private List<FilterUserVo> list;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;
}
