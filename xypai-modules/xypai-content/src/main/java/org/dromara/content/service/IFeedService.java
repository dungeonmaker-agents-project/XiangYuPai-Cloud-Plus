package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.content.domain.dto.FeedListQueryDTO;
import org.dromara.content.domain.dto.FeedPublishDTO;
import org.dromara.content.domain.dto.UserFeedQueryDTO;
import org.dromara.content.domain.vo.FeedDetailVO;
import org.dromara.content.domain.vo.FeedListVO;

/**
 * 动态服务接口
 *
 * @author XiangYuPai
 */
public interface IFeedService {

    /**
     * 获取动态列表
     *
     * @param queryDTO 查询参数
     * @param currentUserId 当前用户ID(可为null)
     * @return 动态列表
     */
    Page<FeedListVO> getFeedList(FeedListQueryDTO queryDTO, Long currentUserId);

    /**
     * 获取动态详情
     *
     * @param feedId 动态ID
     * @param userId 当前用户ID
     * @return 动态详情
     */
    FeedDetailVO getFeedDetail(Long feedId, Long userId);

    /**
     * 发布动态
     *
     * @param publishDTO 发布参数
     * @param userId 用户ID
     * @return 动态ID
     */
    Long publishFeed(FeedPublishDTO publishDTO, Long userId);

    /**
     * 删除动态
     *
     * @param feedId 动态ID
     * @param userId 用户ID
     */
    void deleteFeed(Long feedId, Long userId);

    /**
     * 获取用户动态列表
     *
     * @param targetUserId 目标用户ID
     * @param queryDTO 查询参数
     * @param currentUserId 当前用户ID(可为null)
     * @return 动态列表
     */
    Page<FeedListVO> getUserFeedList(Long targetUserId, UserFeedQueryDTO queryDTO, Long currentUserId);

}
