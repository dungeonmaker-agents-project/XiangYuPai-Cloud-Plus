package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易记录Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

}
