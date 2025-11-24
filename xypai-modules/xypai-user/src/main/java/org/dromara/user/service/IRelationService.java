package org.dromara.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.domain.entity.UserRelation;
import org.dromara.user.domain.dto.UserBlockDto;
import org.dromara.user.domain.dto.UserReportDto;
import org.dromara.user.domain.vo.UserRelationVo;

/**
 * 用户关系服务接口
 * User Relation Service Interface
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
public interface IRelationService extends IService<UserRelation> {

    /**
     * 关注用户
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 结果
     */
    R<Void> followUser(Long followerId, Long followingId);

    /**
     * 取消关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 结果
     */
    R<Void> unfollowUser(Long followerId, Long followingId);

    /**
     * 获取粉丝列表
     *
     * @param userId    用户ID
     * @param keyword   搜索关键词
     * @param pageQuery 分页参数
     * @return 粉丝列表
     */
    TableDataInfo<UserRelationVo> getFansList(Long userId, String keyword, PageQuery pageQuery);

    /**
     * 获取关注列表
     *
     * @param userId    用户ID
     * @param keyword   搜索关键词
     * @param pageQuery 分页参数
     * @return 关注列表
     */
    TableDataInfo<UserRelationVo> getFollowingList(Long userId, String keyword, PageQuery pageQuery);

    /**
     * 拉黑用户
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 结果
     */
    R<Void> blockUser(Long userId, UserBlockDto dto);

    /**
     * 取消拉黑
     *
     * @param userId        用户ID
     * @param blockedUserId 被拉黑用户ID
     * @return 结果
     */
    R<Void> unblockUser(Long userId, Long blockedUserId);

    /**
     * 举报用户
     *
     * @param reporterId 举报人ID
     * @param dto        DTO
     * @return 结果
     */
    R<Void> reportUser(Long reporterId, UserReportDto dto);

    /**
     * 检查关注状态
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 关注状态: none, following, mutual
     */
    String getFollowStatus(Long followerId, Long followingId);

    /**
     * 检查隐私权限
     *
     * @param userId       当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否有权限
     */
    boolean checkPrivacy(Long userId, Long targetUserId);
}
