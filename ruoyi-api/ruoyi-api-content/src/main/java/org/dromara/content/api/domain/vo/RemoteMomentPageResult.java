package org.dromara.content.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 动态分页结果VO (用于RPC传输)
 *
 * <p>用途：对方主页的动态Tab分页展示</p>
 * <p>对应UI文档: 对方主页_结构文档.md - MomentsListData</p>
 *
 * @author XyPai Team
 * @date 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteMomentPageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态列表
     */
    private List<RemoteMomentVo> list;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
