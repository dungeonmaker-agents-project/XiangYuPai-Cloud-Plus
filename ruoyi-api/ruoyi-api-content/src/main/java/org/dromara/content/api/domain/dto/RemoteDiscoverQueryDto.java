package org.dromara.content.api.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 发现页列表查询DTO
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
public class RemoteDiscoverQueryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Tab类型: follow(关注), hot(热门), nearby(同城)
     */
    private String tab;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 纬度（同城Tab必填）
     */
    private BigDecimal latitude;

    /**
     * 经度（同城Tab必填）
     */
    private BigDecimal longitude;

    /**
     * 城市ID（可选，用于同城筛选）
     */
    private Long cityId;

    /**
     * 默认值处理
     */
    public Integer getPageNum() {
        return pageNum != null ? pageNum : 1;
    }

    public Integer getPageSize() {
        return pageSize != null ? pageSize : 20;
    }

    public String getTab() {
        return tab != null ? tab : "hot";
    }
}
