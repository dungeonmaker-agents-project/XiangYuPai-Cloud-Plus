package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
