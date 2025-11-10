package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.OccupationDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 职业字典Mapper接口
 *
 * @author Bob
 * @date 2025-01-14
 */
@Mapper
public interface OccupationDictMapper extends BaseMapper<OccupationDict> {

    /**
     * 查询启用的职业列表（按排序）
     */
    @Select("SELECT * FROM occupation_dict WHERE status = 1 " +
            "ORDER BY sort_order ASC, code ASC")
    List<OccupationDict> selectEnabledOccupations();

    /**
     * 根据分类查询职业
     */
    @Select("SELECT * FROM occupation_dict WHERE category = #{category} AND status = 1 " +
            "ORDER BY sort_order ASC")
    List<OccupationDict> selectByCategory(@Param("category") String category);

    /**
     * 查询所有分类
     */
    @Select("SELECT DISTINCT category FROM occupation_dict WHERE status = 1 " +
            "ORDER BY category ASC")
    List<String> selectAllCategories();

    /**
     * 批量查询职业
     */
    @Select("<script>" +
            "SELECT * FROM occupation_dict WHERE code IN " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            "</script>")
    List<OccupationDict> selectBatchByCodes(@Param("codes") List<String> codes);
}

