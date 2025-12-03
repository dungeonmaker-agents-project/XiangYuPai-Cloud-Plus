package org.dromara.content.api;

import org.dromara.content.api.domain.dto.RemoteDiscoverLikeDto;
import org.dromara.content.api.domain.dto.RemoteDiscoverQueryDto;
import org.dromara.content.api.domain.vo.RemoteDiscoverLikeResultVo;
import org.dromara.content.api.domain.vo.RemoteDiscoverPageResult;

/**
 * 发现页远程服务接口
 * 提供发现页内容列表查询和互动功能
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
public interface RemoteDiscoverService {

    /**
     * 查询发现页内容列表
     * 支持三种Tab: follow(关注), hot(热门), nearby(同城)
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID（可为null表示未登录）
     * @return 分页结果
     */
    RemoteDiscoverPageResult queryDiscoverList(RemoteDiscoverQueryDto queryDTO, Long userId);

    /**
     * 点赞/取消点赞
     *
     * @param likeDto 点赞请求
     * @param userId  当前用户ID
     * @return 点赞结果
     */
    RemoteDiscoverLikeResultVo toggleLike(RemoteDiscoverLikeDto likeDto, Long userId);

    /**
     * 批量检查点赞状态
     *
     * @param feedIds 动态ID列表
     * @param userId  当前用户ID
     * @return feedId -> isLiked 的映射
     */
    java.util.Map<Long, Boolean> batchCheckLikeStatus(java.util.List<Long> feedIds, Long userId);

}
