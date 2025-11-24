package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.LimitedTimeQueryDTO;
import org.dromara.appbff.domain.vo.LimitedTimeResultVO;

/**
 * 限时专享服务接口
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
public interface HomeLimitedTimeService {

    /**
     * 获取限时专享列表
     *
     * @param queryDTO 查询参数
     * @return 限时专享结果
     */
    LimitedTimeResultVO getLimitedTimeList(LimitedTimeQueryDTO queryDTO);
}
