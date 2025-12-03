package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.WechatUnlockConfig;

import java.math.BigDecimal;

/**
 * 微信解锁配置Mapper
 * WeChat Unlock Config Mapper
 *
 * <p>注意: 当前表结构为通用配置表，未来可扩展为用户级别配置</p>
 *
 * @author XyPai Team
 * @since 2025-12-02
 */
@Mapper
public interface WechatUnlockConfigMapper extends BaseMapper<WechatUnlockConfig> {

    /**
     * 获取用户的解锁价格
     *
     * <p>当前实现: 返回null，使用默认价格</p>
     * <p>TODO: 未来支持用户自定义价格时，需要修改表结构</p>
     *
     * @param userId 用户ID
     * @return 解锁价格，如果用户未配置返回null
     */
    default BigDecimal selectUnlockPrice(@Param("userId") Long userId) {
        // 当前表结构不支持用户级别配置，返回null使用默认价格
        return null;
    }

    /**
     * 检查用户是否开启了解锁功能
     *
     * <p>当前实现: 默认返回true（所有用户都开启）</p>
     *
     * @param userId 用户ID
     * @return 是否开启
     */
    default boolean isUnlockEnabled(@Param("userId") Long userId) {
        // 默认所有用户都开启解锁功能
        return true;
    }
}
