package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.HomeFeedResultVO;

/**
 * 首页用户推荐 Feed 服务接口
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
public interface HomeFeedService {

    /**
     * 获取首页用户推荐列表
     *
     * @param queryDTO 查询参数
     * @return 用户推荐结果
     */
    HomeFeedResultVO getHomeFeedList(HomeFeedQueryDTO queryDTO);
}
