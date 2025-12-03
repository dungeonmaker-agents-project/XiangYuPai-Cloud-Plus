package org.dromara.content.service;

import org.dromara.content.domain.dto.FeedDraftDTO;
import org.dromara.content.domain.vo.FeedDraftVO;

import java.util.List;

/**
 * 动态草稿服务接口
 *
 * @author XiangYuPai
 */
public interface IFeedDraftService {

    /**
     * 保存草稿
     *
     * @param draftDTO 草稿数据
     * @param userId   用户ID
     * @return 草稿ID
     */
    Long saveDraft(FeedDraftDTO draftDTO, Long userId);

    /**
     * 获取用户草稿列表
     *
     * @param userId 用户ID
     * @return 草稿列表
     */
    List<FeedDraftVO> getUserDrafts(Long userId);

    /**
     * 获取草稿详情
     *
     * @param draftId 草稿ID
     * @param userId  用户ID
     * @return 草稿详情
     */
    FeedDraftVO getDraftDetail(Long draftId, Long userId);

    /**
     * 删除草稿
     *
     * @param draftId 草稿ID
     * @param userId  用户ID
     */
    void deleteDraft(Long draftId, Long userId);

}
