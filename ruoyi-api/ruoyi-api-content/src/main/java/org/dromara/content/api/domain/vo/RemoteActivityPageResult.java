package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 活动分页结果VO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityPageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动列表
     */
    private List<RemoteActivityVo> list;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 统计信息
     */
    private FilterStats filterStats;

    @Data
    public static class FilterStats implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 总活动数
         */
        private Integer totalActivities;

        /**
         * 今日新增
         */
        private Integer todayNew;

        /**
         * 本周热门
         */
        private Integer weeklyHot;
    }
}
