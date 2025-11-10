package com.xypai.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.content.domain.dto.ContentAddDTO;
import com.xypai.content.domain.dto.ContentDraftSaveDTO;
import com.xypai.content.domain.entity.Content;
import com.xypai.content.domain.entity.ContentDraft;
import com.xypai.content.domain.vo.ContentDraftVO;
import com.xypai.content.handler.PointTypeHandler;
import com.xypai.content.mapper.ContentDraftMapper;
import com.xypai.content.service.IContentDraftService;
import com.xypai.content.service.IContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容草稿服务实现�?
 * 
 * 自动保存机制�?
 * 1. �?0秒自动保存一�?
 * 2. 使用upsert模式（存在则更新，不存在则插入）
 * 3. 30天后自动过期
 * 4. 用户最多保�?0个草�?
 *
 * @author David (内容服务�?
 * @date 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentDraftServiceImpl implements IContentDraftService {

    private final ContentDraftMapper contentDraftMapper;
    private final IContentService contentService;

    /**
     * 草稿过期天数
     */
    private static final int DRAFT_EXPIRE_DAYS = 30;

    /**
     * 用户最多草稿数
     */
    private static final int MAX_DRAFTS_PER_USER = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(ContentDraftSaveDTO draftSaveDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 创建或更新草�?
        ContentDraft draft;

        if (draftSaveDTO.getId() != null) {
            // 更新现有草稿
            draft = contentDraftMapper.selectById(draftSaveDTO.getId());
            if (draft == null) {
                throw new ServiceException("草稿不存在");
            }
            if (!currentUserId.equals(draft.getUserId())) {
                throw new ServiceException("无权限修改该草稿");
            }

            updateDraftFromDTO(draft, draftSaveDTO);
            draft.setAutoSaveTime(LocalDateTime.now());
            draft.setUpdatedAt(LocalDateTime.now());

            contentDraftMapper.updateById(draft);
            log.debug("更新草稿, draftId={}", draft.getId());

        } else {
            // 创建新草�?
            // 检查草稿数量限制
            Long draftCount = contentDraftMapper.countByUserId(currentUserId);
            if (draftCount >= MAX_DRAFTS_PER_USER) {
                throw new ServiceException("草稿数量已达上限（" + MAX_DRAFTS_PER_USER + "个）");
            }

            draft = buildDraftFromDTO(draftSaveDTO, currentUserId);
            contentDraftMapper.insert(draft);
            log.info("创建草稿, draftId={}, userId={}", draft.getId(), currentUserId);
        }

        return draft.getId();
    }

    @Override
    public ContentDraftVO getDraftById(Long draftId) {
        if (draftId == null) {
            throw new ServiceException("草稿ID不能为空");
        }

        ContentDraft draft = contentDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new ServiceException("草稿不存在");
        }

        // 权限检查
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null || !currentUserId.equals(draft.getUserId())) {
            throw new ServiceException("无权限查看该草稿");
        }

        return convertToVO(draft);
    }

    @Override
    public List<ContentDraftVO> getUserDrafts(Integer limit) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        Integer searchLimit = limit != null ? limit : 10;

        List<ContentDraft> drafts = contentDraftMapper.selectDraftsByUserId(currentUserId, searchLimit);

        return drafts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDraft(Long draftId) {
        if (draftId == null) {
            throw new ServiceException("草稿ID不能为空");
        }

        ContentDraft draft = contentDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new ServiceException("草稿不存在");
        }

        // 权限检查
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null || !currentUserId.equals(draft.getUserId())) {
            throw new ServiceException("无权限删除该草稿");
        }

        int result = contentDraftMapper.deleteById(draftId);
        log.info("删除草稿成功, draftId={}", draftId);

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishDraft(Long draftId) {
        if (draftId == null) {
            throw new ServiceException("草稿ID不能为空");
        }

        ContentDraft draft = contentDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new ServiceException("草稿不存在");
        }

        // 权限检查
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null || !currentUserId.equals(draft.getUserId())) {
            throw new ServiceException("无权限发布该草稿");
        }

        // 转换为ContentAddDTO
        ContentAddDTO contentAddDTO = convertDraftToContentDTO(draft);

        // 创建正式内容
        boolean success = contentService.createContent(contentAddDTO);
        if (!success) {
            throw new ServiceException("发布草稿失败");
        }

        // 标记草稿为已发布
        contentDraftMapper.markAsPublished(draftId);

        log.info("发布草稿成功, draftId={}", draftId);
        return draftId;
    }

    /**
     * 定时任务：每天凌�?点清理过期草�?
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredDrafts() {
        try {
            log.info("开始清理过期草�?..");
            int deletedCount = contentDraftMapper.deleteExpiredDrafts();
            log.info("清理过期草稿完成, 删除数量={}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期草稿失败", e);
        }
    }

    @Override
    public Long countUserDrafts() {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            return 0L;
        }

        return contentDraftMapper.countByUserId(currentUserId);
    }

    /**
     * 从DTO构建草稿对象
     */
    private ContentDraft buildDraftFromDTO(ContentDraftSaveDTO dto, Long userId) {
        Point location = null;
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            location = PointTypeHandler.createPoint(dto.getLongitude(), dto.getLatitude());
        }

        return ContentDraft.builder()
                .userId(userId)
                .type(dto.getType())
                .title(dto.getTitle())
                .content(dto.getContent())
                .coverUrl(dto.getCoverUrl())
                .locationName(dto.getLocationName())
                .locationAddress(dto.getLocationAddress())
                .location(location)
                .cityId(dto.getCityId())
                .data(dto.getData())
                .autoSaveTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(DRAFT_EXPIRE_DAYS))
                .status(ContentDraft.Status.EDITING.getCode())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 从DTO更新草稿对象
     */
    private void updateDraftFromDTO(ContentDraft draft, ContentDraftSaveDTO dto) {
        if (dto.getTitle() != null) {
            draft.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            draft.setContent(dto.getContent());
        }
        if (dto.getCoverUrl() != null) {
            draft.setCoverUrl(dto.getCoverUrl());
        }
        if (dto.getLocationName() != null) {
            draft.setLocationName(dto.getLocationName());
        }
        if (dto.getLocationAddress() != null) {
            draft.setLocationAddress(dto.getLocationAddress());
        }
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            draft.setLocation(PointTypeHandler.createPoint(dto.getLongitude(), dto.getLatitude()));
        }
        if (dto.getCityId() != null) {
            draft.setCityId(dto.getCityId());
        }
        if (dto.getData() != null) {
            draft.setData(dto.getData());
        }
    }

    /**
     * 转换为VO
     */
    private ContentDraftVO convertToVO(ContentDraft draft) {
        Double longitude = null;
        Double latitude = null;
        if (draft.getLocation() != null) {
            longitude = PointTypeHandler.getLongitude(draft.getLocation());
            latitude = PointTypeHandler.getLatitude(draft.getLocation());
        }

        return ContentDraftVO.builder()
                .id(draft.getId())
                .type(draft.getType())
                .typeDesc(getTypeDesc(draft.getType()))
                .title(draft.getTitle())
                .content(draft.getContent())
                .coverUrl(draft.getCoverUrl())
                .locationName(draft.getLocationName())
                .locationAddress(draft.getLocationAddress())
                .longitude(longitude)
                .latitude(latitude)
                .cityId(draft.getCityId())
                .data(draft.getData())
                .autoSaveTime(draft.getAutoSaveTime())
                .expireTime(draft.getExpireTime())
                .status(draft.getStatus())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    /**
     * 转换草稿为ContentAddDTO
     */
    private ContentAddDTO convertDraftToContentDTO(ContentDraft draft) {
        // TODO: 需要根据实际ContentAddDTO字段调整
        return ContentAddDTO.builder()
                .type(draft.getType())
                .title(draft.getTitle())
                .publish(true)
                .build();
    }

    /**
     * 获取类型描述
     */
    private String getTypeDesc(Integer type) {
        Content.Type contentType = Content.Type.fromCode(type);
        return contentType != null ? contentType.getDesc() : "未知";
    }
}

