package org.dromara.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.order.domain.entity.Order;

/**
 * 订单Mapper
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
