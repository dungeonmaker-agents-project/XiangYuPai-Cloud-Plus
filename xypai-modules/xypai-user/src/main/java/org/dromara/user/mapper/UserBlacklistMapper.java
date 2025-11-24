package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.UserBlacklist;

import java.util.List;

/**
 * 用户黑名单Mapper
 * User Blacklist Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface UserBlacklistMapper extends BaseMapper<UserBlacklist> {

    /**
     * 查询是否已拉黑
     *
     * @param userId        用户ID
     * @param blockedUserId 被拉黑用户ID
     * @return 黑名单记录
     */
    @Select("SELECT * FROM user_blacklist WHERE user_id = #{userId} AND blocked_user_id = #{blockedUserId} AND deleted = 0 LIMIT 1")
    UserBlacklist selectBlacklist(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);

    /**
     * 查询用户的所有黑名单
     *
     * @param userId 用户ID
     * @return 被拉黑的用户ID列表
     */
    @Select("SELECT blocked_user_id FROM user_blacklist WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectBlockedUsers(@Param("userId") Long userId);

    /**
     * 检查两个用户是否互相拉黑
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 是否有拉黑关系
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM user_blacklist
        WHERE ((user_id = #{userId1} AND blocked_user_id = #{userId2})
           OR (user_id = #{userId2} AND blocked_user_id = #{userId1}))
        AND deleted = 0
        """)
    boolean hasBlacklist(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询黑名单记录 (别名方法)
     *
     * @param userId        用户ID
     * @param blockedUserId 被拉黑用户ID
     * @return 黑名单记录
     */
    default UserBlacklist selectByUserAndBlocked(Long userId, Long blockedUserId) {
        return selectBlacklist(userId, blockedUserId);
    }
}
