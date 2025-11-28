package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.content.domain.entity.ActivityType;

import java.util.List;

/**
 * 活动类型Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface ActivityTypeMapper extends BaseMapper<ActivityType> {

    /**
     * 查询所有启用的活动类型(按排序)
     *
     * @return 活动类型列表
     */
    @Select("""
        SELECT * FROM activity_type
        WHERE status = 1
          AND deleted = 0
        ORDER BY sort_order ASC
        """)
    List<ActivityType> selectAllEnabled();

    /**
     * 查询热门活动类型
     *
     * @return 热门活动类型列表
     */
    @Select("""
        SELECT * FROM activity_type
        WHERE status = 1
          AND is_hot = 1
          AND deleted = 0
        ORDER BY sort_order ASC
        """)
    List<ActivityType> selectHotTypes();

    /**
     * 根据类型编码查询
     *
     * @param typeCode 类型编码
     * @return 活动类型
     */
    @Select("""
        SELECT * FROM activity_type
        WHERE type_code = #{typeCode}
          AND deleted = 0
        LIMIT 1
        """)
    ActivityType selectByTypeCode(@Param("typeCode") String typeCode);
}
