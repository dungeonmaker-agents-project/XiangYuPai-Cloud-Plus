package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.WechatUnlock;

/**
 * 微信解锁记录Mapper
 * WeChat Unlock Mapper
 *
 * @author XyPai Team
 * @since 2025-12-02
 */
@Mapper
public interface WechatUnlockMapper extends BaseMapper<WechatUnlock> {

    /**
     * 查询是否已解锁
     *
     * @param userId       解锁者ID
     * @param targetUserId 被解锁者ID
     * @return 解锁记录
     */
    @Select("""
        SELECT * FROM wechat_unlocks
        WHERE user_id = #{userId}
        AND target_user_id = #{targetUserId}
        AND deleted = 0
        LIMIT 1
        """)
    WechatUnlock selectUnlockRecord(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);

    /**
     * 检查是否已解锁
     *
     * @param userId       解锁者ID
     * @param targetUserId 被解锁者ID
     * @return 是否已解锁
     */
    @Select("""
        SELECT EXISTS(
            SELECT 1 FROM wechat_unlocks
            WHERE user_id = #{userId}
            AND target_user_id = #{targetUserId}
            AND deleted = 0
        )
        """)
    boolean existsUnlock(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);

    /**
     * 统计用户解锁次数
     *
     * @param userId 用户ID
     * @return 解锁次数
     */
    @Select("SELECT COUNT(*) FROM wechat_unlocks WHERE user_id = #{userId} AND deleted = 0")
    long countUnlocksByUserId(@Param("userId") Long userId);
}
