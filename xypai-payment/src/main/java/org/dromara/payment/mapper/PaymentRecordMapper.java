package org.dromara.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.payment.domain.entity.PaymentRecord;

/**
 * 支付记录Mapper
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

}
