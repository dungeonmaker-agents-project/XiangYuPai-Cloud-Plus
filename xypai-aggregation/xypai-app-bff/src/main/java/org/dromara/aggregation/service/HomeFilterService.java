package org.dromara.aggregation.service;

import org.dromara.aggregation.domain.dto.FilterApplyDTO;
import org.dromara.aggregation.domain.vo.FilterConfigVO;
import org.dromara.aggregation.domain.vo.FilterResultVO;

/**
 * 首页筛选服务接口
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
public interface HomeFilterService {

    /**
     * 获取筛选配置
     *
     * @param type 类型: online-线上, offline-线下
     * @return 筛选配置
     */
    FilterConfigVO getFilterConfig(String type);

    /**
     * 应用筛选条件
     *
     * @param dto 筛选条件DTO
     * @return 筛选结果
     */
    FilterResultVO applyFilter(FilterApplyDTO dto);
}
