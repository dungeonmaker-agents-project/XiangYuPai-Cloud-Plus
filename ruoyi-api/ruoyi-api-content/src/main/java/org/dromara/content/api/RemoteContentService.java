package org.dromara.content.api;

import org.dromara.content.api.domain.vo.UserFeedsVo;

import java.util.List;
import java.util.Map;

/**
 * 内容服务远程接口
 *
 * <p>用途：供其他服务通过Dubbo调用内容相关功能</p>
 * <p>实现：xypai-content模块实现此接口</p>
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
public interface RemoteContentService {

    /**
     * 批量获取用户最新动态
     *
     * <p>用途：首页用户卡片下方展示用户最新动态</p>
     * <p>特点：</p>
     * <ul>
     *     <li>支持批量查询多个用户</li>
     *     <li>每个用户最多返回指定数量的动态</li>
     *     <li>按创建时间倒序排列</li>
     *     <li>只返回公开的、正常状态的动态</li>
     * </ul>
     *
     * @param userIds 用户ID列表
     * @param limit   每个用户返回的动态数量上限（建议3）
     * @return Map<用户ID, 用户动态列表>
     */
    Map<Long, UserFeedsVo> batchGetUserFeeds(List<Long> userIds, Integer limit);

    /**
     * 获取单个用户的最新动态
     *
     * @param userId 用户ID
     * @param limit  返回的动态数量上限
     * @return 用户动态列表VO
     */
    UserFeedsVo getUserFeeds(Long userId, Integer limit);

    /**
     * 获取用户动态总数
     *
     * @param userId 用户ID
     * @return 动态总数
     */
    Integer getUserFeedCount(Long userId);

    /**
     * 批量获取用户动态总数
     *
     * @param userIds 用户ID列表
     * @return Map<用户ID, 动态总数>
     */
    Map<Long, Integer> batchGetUserFeedCounts(List<Long> userIds);
}
