package org.dromara.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.payment.domain.entity.UserAccount;

/**
 * 用户账户Mapper
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

}
