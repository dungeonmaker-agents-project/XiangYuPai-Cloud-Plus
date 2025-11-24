package org.dromara.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.payment.domain.entity.AccountTransaction;

/**
 * 账户流水Mapper
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Mapper
public interface AccountTransactionMapper extends BaseMapper<AccountTransaction> {

}
