package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.Media;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 媒体文件Mapper接口
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Mapper
public interface MediaMapper extends BaseMapper<Media> {

    /**
     * 查询关联对象的媒体列表
     *
     * @param refType 关联类型（content/draft/comment/profile/message）
     * @param refId 关联对象ID
     * @return 媒体列表
     */
    List<Media> selectByRef(@Param("refType") String refType, 
                            @Param("refId") Long refId);

    /**
     * 批量查询关联对象的媒体
     *
     * @param refType 关联类型
     * @param refIds 关联对象ID列表
     * @return 媒体列表
     */
    List<Media> selectBatchByRef(@Param("refType") String refType, 
                                 @Param("refIds") List<Long> refIds);

    /**
     * 删除关联对象的媒体
     *
     * @param refType 关联类型
     * @param refId 关联对象ID
     * @return 影响行数
     */
    int deleteByRef(@Param("refType") String refType, 
                    @Param("refId") Long refId);

    /**
     * 统计用户上传的媒体数量
     *
     * @param uploaderId 上传者ID
     * @return 媒体数量
     */
    Long countByUploaderId(@Param("uploaderId") Long uploaderId);
}

