package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 发现页列表分页结果
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
public class RemoteDiscoverPageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容列表
     */
    private List<RemoteDiscoverFeedVo> list;

    /**
     * 是否有更多
     */
    private Boolean hasMore;

    /**
     * 总数
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

    /**
     * 总页数
     */
    private Integer pages;

}
