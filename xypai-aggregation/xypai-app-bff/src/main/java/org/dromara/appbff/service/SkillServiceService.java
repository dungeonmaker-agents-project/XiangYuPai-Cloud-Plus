package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.ServiceListQueryDTO;
import org.dromara.appbff.domain.dto.ServiceReviewQueryDTO;
import org.dromara.appbff.domain.vo.ServiceDetailVO;
import org.dromara.appbff.domain.vo.ServiceListResultVO;
import org.dromara.appbff.domain.vo.ServiceReviewListVO;

/**
 * 技能服务服务接口
 * 对应前端: 11-服务列表页面, 12-服务详情页面
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
public interface SkillServiceService {

    /**
     * 查询服务列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID
     * @return 服务列表结果
     */
    ServiceListResultVO queryServiceList(ServiceListQueryDTO queryDTO, Long userId);

    /**
     * 获取服务详情
     *
     * @param serviceId 服务ID
     * @param userId    当前用户ID
     * @return 服务详情
     */
    ServiceDetailVO getServiceDetail(Long serviceId, Long userId);

    /**
     * 获取服务评价列表
     *
     * @param queryDTO 查询条件
     * @return 评价列表
     */
    ServiceReviewListVO getServiceReviews(ServiceReviewQueryDTO queryDTO);
}
