package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.user.domain.entity.UserReport;

/**
 * 用户举报Mapper
 * User Report Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface UserReportMapper extends BaseMapper<UserReport> {
    // MyBatis Plus提供基础CRUD操作
}
