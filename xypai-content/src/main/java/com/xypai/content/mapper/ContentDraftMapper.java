package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.ContentDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容草稿Mapper接口
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Mapper
public interface ContentDraftMapper extends BaseMapper<ContentDraft> {

    /**
     * 查询用户的草稿列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 草稿列表（按更新时间倒序）
     */
    List<ContentDraft> selectDraftsByUserId(@Param("userId") Long userId, 
                                            @Param("limit") Integer limit);

    /**
     * 删除过期草稿
     *
     * @return 删除数量
     */
    int deleteExpiredDrafts();

    /**
     * 统计用户的草稿数量
     *
     * @param userId 用户ID
     * @return 草稿数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 标记草稿为已发布
     *
     * @param draftId 草稿ID
     * @return 影响行数
     */
    int markAsPublished(@Param("draftId") Long draftId);
}

