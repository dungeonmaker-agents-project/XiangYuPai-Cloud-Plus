package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.content.domain.dto.DiscoverListQueryDTO;
import org.dromara.content.domain.vo.DiscoverFeedVO;

import java.util.List;
import java.util.Map;

/**
 * 发现页服务接口
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
public interface IDiscoverService {

    /**
     * 查询发现页内容列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID（可为null表示未登录）
     * @return 分页结果
     */
    Page<DiscoverFeedVO> queryDiscoverList(DiscoverListQueryDTO queryDTO, Long userId);

    /**
     * 点赞/取消点赞
     *
     * @param feedId  动态ID
     * @param userId  用户ID
     * @param isLike  true=点赞, false=取消点赞
     * @return 新的点赞数
     */
    Integer toggleLike(Long feedId, Long userId, boolean isLike);

    /**
     * 批量检查点赞状态
     *
     * @param feedIds 动态ID列表
     * @param userId  用户ID
     * @return feedId -> isLiked
     */
    Map<Long, Boolean> batchCheckLikeStatus(List<Long> feedIds, Long userId);

    /**
     * 获取单个动态的点赞数
     *
     * @param feedId 动态ID
     * @return 点赞数
     */
    Integer getLikeCount(Long feedId);

}
