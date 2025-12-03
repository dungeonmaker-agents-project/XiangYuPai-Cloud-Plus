package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.content.domain.dto.FeedDraftDTO;
import org.dromara.content.domain.entity.FeedDraft;
import org.dromara.content.domain.vo.FeedDraftVO;
import org.dromara.content.mapper.FeedDraftMapper;
import org.dromara.content.service.IFeedDraftService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态草稿服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedDraftServiceImpl implements IFeedDraftService {

    private final FeedDraftMapper feedDraftMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Long saveDraft(FeedDraftDTO draftDTO, Long userId) {
        FeedDraft draft;

        if (draftDTO.getId() != null) {
            // 更新现有草稿
            draft = feedDraftMapper.selectById(draftDTO.getId());
            if (draft == null || !draft.getUserId().equals(userId)) {
                throw new ServiceException("草稿不存在或无权限");
            }
        } else {
            // 创建新草稿
            draft = new FeedDraft();
            draft.setUserId(userId);
        }

        // 设置草稿内容
        draft.setTitle(draftDTO.getTitle());
        draft.setContent(draftDTO.getContent());
        draft.setMediaList(toJson(draftDTO.getMediaList()));
        draft.setTopicList(toJson(draftDTO.getTopicList()));
        draft.setLocationId(draftDTO.getLocationId());
        draft.setLocationName(draftDTO.getLocationName());
        draft.setLocationAddress(draftDTO.getLocationAddress());
        draft.setLongitude(draftDTO.getLongitude());
        draft.setLatitude(draftDTO.getLatitude());
        draft.setVisibility(draftDTO.getVisibility() != null ? draftDTO.getVisibility() : 0);

        if (draftDTO.getId() != null) {
            feedDraftMapper.updateById(draft);
        } else {
            feedDraftMapper.insert(draft);
        }

        log.info("保存草稿成功, draftId: {}, userId: {}", draft.getId(), userId);
        return draft.getId();
    }

    @Override
    public List<FeedDraftVO> getUserDrafts(Long userId) {
        List<FeedDraft> drafts = feedDraftMapper.selectList(
            new LambdaQueryWrapper<FeedDraft>()
                .eq(FeedDraft::getUserId, userId)
                .orderByDesc(FeedDraft::getUpdatedAt)
        );

        return drafts.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public FeedDraftVO getDraftDetail(Long draftId, Long userId) {
        FeedDraft draft = feedDraftMapper.selectById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            throw new ServiceException("草稿不存在或无权限");
        }
        return convertToVO(draft);
    }

    @Override
    public void deleteDraft(Long draftId, Long userId) {
        FeedDraft draft = feedDraftMapper.selectById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            throw new ServiceException("草稿不存在或无权限");
        }
        feedDraftMapper.deleteById(draftId);
        log.info("删除草稿成功, draftId: {}, userId: {}", draftId, userId);
    }

    /**
     * 转换实体到VO
     */
    private FeedDraftVO convertToVO(FeedDraft draft) {
        return FeedDraftVO.builder()
            .id(draft.getId())
            .title(draft.getTitle())
            .content(draft.getContent())
            .mediaList(fromJson(draft.getMediaList()))
            .topicList(fromJson(draft.getTopicList()))
            .locationId(draft.getLocationId())
            .locationName(draft.getLocationName())
            .locationAddress(draft.getLocationAddress())
            .longitude(draft.getLongitude())
            .latitude(draft.getLatitude())
            .visibility(draft.getVisibility())
            .updatedAt(draft.getUpdatedAt())
            .build();
    }

    /**
     * 对象转JSON字符串
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转对象
     */
    private List<Map<String, Object>> fromJson(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            return Collections.emptyList();
        }
    }

}
