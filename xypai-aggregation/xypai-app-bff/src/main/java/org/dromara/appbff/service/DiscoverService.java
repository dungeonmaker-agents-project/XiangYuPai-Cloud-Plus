package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.DiscoverLikeDTO;
import org.dromara.appbff.domain.dto.DiscoverListQueryDTO;
import org.dromara.appbff.domain.vo.DiscoverLikeResultVO;
import org.dromara.appbff.domain.vo.DiscoverListResultVO;

/**
 * 发现页服务接口
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
public interface DiscoverService {

    /**
     * 查询发现页内容列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID（可为null表示未登录）
     * @return 列表结果
     */
    DiscoverListResultVO queryDiscoverList(DiscoverListQueryDTO queryDTO, Long userId);

    /**
     * 点赞/取消点赞
     *
     * @param likeDTO 点赞请求
     * @param userId  当前用户ID
     * @return 点赞结果
     */
    DiscoverLikeResultVO toggleLike(DiscoverLikeDTO likeDTO, Long userId);

}
