package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.content.domain.entity.Report;

/**
 * 举报Mapper接口
 *
 * @author XiangYuPai
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

}
