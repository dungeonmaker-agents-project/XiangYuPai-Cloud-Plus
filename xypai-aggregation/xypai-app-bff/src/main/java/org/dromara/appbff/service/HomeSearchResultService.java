package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.SearchQueryDTO;
import org.dromara.appbff.domain.vo.*;

/**
 * 首页搜索结果服务接口
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
public interface HomeSearchResultService {

    /**
     * 执行搜索（综合）
     * - 根据type返回对应Tab的结果
     * - 同时返回所有Tab的统计信息
     *
     * @param queryDTO 搜索查询参数
     * @return 搜索结果
     */
    SearchResultVO search(SearchQueryDTO queryDTO);

    /**
     * 获取全部Tab结果
     * - 返回混合结果（动态、视频、用户）
     *
     * @param queryDTO 搜索查询参数
     * @return 全部Tab结果
     */
    SearchAllResultVO searchAll(SearchQueryDTO queryDTO);

    /**
     * 获取用户Tab结果
     * - 返回匹配的用户列表
     * - 包含关注状态
     *
     * @param queryDTO  搜索查询参数
     * @param currentUserId 当前用户ID（用于判断关注状态）
     * @return 用户Tab结果
     */
    SearchUserResultVO searchUsers(SearchQueryDTO queryDTO, Long currentUserId);

    /**
     * 获取下单Tab结果
     * - 返回匹配的服务提供者列表
     * - 包含距离、价格、标签等信息
     *
     * @param queryDTO 搜索查询参数
     * @return 下单Tab结果
     */
    SearchOrderResultVO searchOrders(SearchQueryDTO queryDTO);

    /**
     * 获取话题Tab结果
     * - 返回匹配的话题列表
     * - 包含话题统计信息
     *
     * @param queryDTO 搜索查询参数
     * @return 话题Tab结果
     */
    SearchTopicResultVO searchTopics(SearchQueryDTO queryDTO);
}
