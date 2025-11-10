package com.xypai.content.service;

import com.xypai.content.domain.dto.ContentDraftSaveDTO;
import com.xypai.content.domain.vo.ContentDraftVO;

import java.util.List;

/**
 * 内容草稿服务接口
 * 
 * 自动保存策略：
 * 1. 前端每10秒调用一次保存接口
 * 2. 草稿30天后自动过期
 * 3. 发布成功后删除草稿
 * 4. 用户最多保留10个草稿
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
public interface IContentDraftService {

    /**
     * 保存草稿（自动保存/手动保存）
     *
     * @param draftSaveDTO 草稿数据
     * @return 草稿ID
     */
    Long saveDraft(ContentDraftSaveDTO draftSaveDTO);

    /**
     * 获取草稿详情
     *
     * @param draftId 草稿ID
     * @return 草稿详情
     */
    ContentDraftVO getDraftById(Long draftId);

    /**
     * 获取用户的草稿列表
     *
     * @param limit 限制数量
     * @return 草稿列表
     */
    List<ContentDraftVO> getUserDrafts(Integer limit);

    /**
     * 删除草稿
     *
     * @param draftId 草稿ID
     * @return 是否成功
     */
    boolean deleteDraft(Long draftId);

    /**
     * 发布草稿（转换为正式内容）
     *
     * @param draftId 草稿ID
     * @return 内容ID
     */
    Long publishDraft(Long draftId);

    /**
     * 清理过期草稿（定时任务调用）
     */
    void cleanExpiredDrafts();

    /**
     * 统计用户草稿数量
     *
     * @return 草稿数量
     */
    Long countUserDrafts();
}

