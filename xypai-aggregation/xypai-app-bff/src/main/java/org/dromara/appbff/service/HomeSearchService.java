package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.SearchHistoryDeleteDTO;
import org.dromara.appbff.domain.dto.SearchSuggestQueryDTO;
import org.dromara.appbff.domain.vo.SearchDeleteVO;
import org.dromara.appbff.domain.vo.SearchInitVO;
import org.dromara.appbff.domain.vo.SearchSuggestVO;

/**
 * 首页搜索服务接口
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
public interface HomeSearchService {

    /**
     * 获取搜索初始数据
     * - 搜索历史（最多10条）
     * - 热门搜索关键词
     * - 搜索框占位符
     *
     * @param userId 当前用户ID（从token获取）
     * @return 搜索初始数据
     */
    SearchInitVO getSearchInit(Long userId);

    /**
     * 获取搜索建议
     * - 根据输入关键词实时返回建议
     * - 包括用户、话题、关键词三种类型
     * - 前端需防抖300ms后调用
     *
     * @param queryDTO 搜索建议查询参数
     * @return 搜索建议列表
     */
    SearchSuggestVO getSearchSuggestions(SearchSuggestQueryDTO queryDTO);

    /**
     * 删除搜索历史
     * - 删除单条历史记录
     * - 或清空所有历史记录
     *
     * @param userId    当前用户ID（从token获取）
     * @param deleteDTO 删除参数
     * @return 删除结果
     */
    SearchDeleteVO deleteSearchHistory(Long userId, SearchHistoryDeleteDTO deleteDTO);
}
