package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.DiscoverLikeDTO;
import org.dromara.appbff.domain.dto.DiscoverListQueryDTO;
import org.dromara.appbff.domain.vo.DiscoverContentItemVO;
import org.dromara.appbff.domain.vo.DiscoverLikeResultVO;
import org.dromara.appbff.domain.vo.DiscoverListResultVO;
import org.dromara.appbff.service.DiscoverService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.model.AppLoginUser;
import org.dromara.content.api.RemoteDiscoverService;
import org.dromara.content.api.domain.dto.RemoteDiscoverLikeDto;
import org.dromara.content.api.domain.dto.RemoteDiscoverQueryDto;
import org.dromara.content.api.domain.vo.RemoteDiscoverFeedVo;
import org.dromara.content.api.domain.vo.RemoteDiscoverLikeResultVo;
import org.dromara.content.api.domain.vo.RemoteDiscoverPageResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 发现页服务实现
 * 聚合 content 服务和 user 服务的数据
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoverServiceImpl implements DiscoverService {

    @DubboReference
    private RemoteDiscoverService remoteDiscoverService;

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @Override
    public DiscoverListResultVO queryDiscoverList(DiscoverListQueryDTO queryDTO, Long userId) {
        log.info("查询发现列表: tab={}, pageNum={}, pageSize={}, userId={}",
            queryDTO.getTab(), queryDTO.getPageNum(), queryDTO.getPageSize(), userId);

        try {
            // 转换查询条件
            RemoteDiscoverQueryDto rpcQuery = new RemoteDiscoverQueryDto();
            rpcQuery.setTab(queryDTO.getTab());
            rpcQuery.setPageNum(queryDTO.getPageNum());
            rpcQuery.setPageSize(queryDTO.getPageSize());
            rpcQuery.setLatitude(queryDTO.getLatitude());
            rpcQuery.setLongitude(queryDTO.getLongitude());

            // 调用 content 服务获取动态列表
            RemoteDiscoverPageResult rpcResult = remoteDiscoverService.queryDiscoverList(rpcQuery, userId);

            // 聚合用户信息
            List<DiscoverContentItemVO> items = aggregateUserInfo(rpcResult.getList());

            // 构建返回结果
            DiscoverListResultVO result = new DiscoverListResultVO();
            result.setList(items);
            result.setHasMore(rpcResult.getHasMore());
            result.setTotal(rpcResult.getTotal());

            return result;
        } catch (Exception e) {
            log.error("查询发现列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            // 返回空结果作为降级
            return createEmptyResult();
        }
    }

    /**
     * 聚合用户信息到内容列表
     */
    private List<DiscoverContentItemVO> aggregateUserInfo(List<RemoteDiscoverFeedVo> feedList) {
        if (feedList == null || feedList.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取所有用户ID
        Set<Long> userIds = feedList.stream()
            .map(RemoteDiscoverFeedVo::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // 批量获取用户信息
        Map<Long, AppLoginUser> userMap = batchGetUserInfo(userIds);

        // 转换为VO并填充用户信息
        return feedList.stream()
            .map(feed -> convertToContentItem(feed, userMap.get(feed.getUserId())))
            .collect(Collectors.toList());
    }

    /**
     * 批量获取用户信息
     */
    private Map<Long, AppLoginUser> batchGetUserInfo(Set<Long> userIds) {
        Map<Long, AppLoginUser> userMap = new HashMap<>();

        for (Long userId : userIds) {
            try {
                AppLoginUser user = remoteAppUserService.getUserById(userId);
                if (user != null) {
                    userMap.put(userId, user);
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        return userMap;
    }

    /**
     * 转换为内容项VO
     */
    private DiscoverContentItemVO convertToContentItem(RemoteDiscoverFeedVo feed, AppLoginUser user) {
        DiscoverContentItemVO item = new DiscoverContentItemVO();
        item.setId(String.valueOf(feed.getId()));
        item.setType(feed.getType());

        // 媒体数据
        DiscoverContentItemVO.MediaData mediaData = new DiscoverContentItemVO.MediaData();
        mediaData.setCoverUrl(feed.getCoverUrl());
        mediaData.setAspectRatio(feed.getAspectRatio());
        mediaData.setDuration(feed.getDuration());
        mediaData.setWidth(feed.getWidth());
        mediaData.setHeight(feed.getHeight());
        item.setMediaData(mediaData);

        // 文本数据
        DiscoverContentItemVO.TextData textData = new DiscoverContentItemVO.TextData();
        textData.setTitle(feed.getTitle());
        textData.setContent(feed.getContent());
        item.setTextData(textData);

        // 作者数据
        DiscoverContentItemVO.AuthorData authorData = new DiscoverContentItemVO.AuthorData();
        authorData.setUserId(String.valueOf(feed.getUserId()));
        if (user != null) {
            authorData.setAvatar(user.getAvatar());
            authorData.setNickname(user.getNickname());
        } else {
            // 降级：使用RPC返回的数据或默认值
            authorData.setAvatar(feed.getUserAvatar() != null ? feed.getUserAvatar() : "");
            authorData.setNickname(feed.getUserNickname() != null ? feed.getUserNickname() : "用户" + feed.getUserId());
        }
        item.setAuthorData(authorData);

        // 统计数据
        DiscoverContentItemVO.StatsData statsData = new DiscoverContentItemVO.StatsData();
        statsData.setLikeCount(feed.getLikeCount() != null ? feed.getLikeCount() : 0);
        statsData.setIsLiked(feed.getIsLiked() != null ? feed.getIsLiked() : false);
        statsData.setCommentCount(feed.getCommentCount() != null ? feed.getCommentCount() : 0);
        statsData.setCollectCount(feed.getCollectCount() != null ? feed.getCollectCount() : 0);
        statsData.setIsCollected(feed.getIsCollected() != null ? feed.getIsCollected() : false);
        item.setStatsData(statsData);

        // 元信息
        DiscoverContentItemVO.MetaData metaData = new DiscoverContentItemVO.MetaData();
        metaData.setCreateTime(feed.getCreateTime());
        metaData.setLocation(feed.getLocation());
        metaData.setDistance(feed.getDistance());
        item.setMetaData(metaData);

        return item;
    }

    @Override
    public DiscoverLikeResultVO toggleLike(DiscoverLikeDTO likeDTO, Long userId) {
        log.info("切换点赞: contentId={}, action={}, userId={}", likeDTO.getContentId(), likeDTO.getAction(), userId);

        try {
            RemoteDiscoverLikeDto rpcDto = new RemoteDiscoverLikeDto();
            rpcDto.setContentId(likeDTO.getContentId());
            rpcDto.setAction(likeDTO.getAction());

            RemoteDiscoverLikeResultVo rpcResult = remoteDiscoverService.toggleLike(rpcDto, userId);

            DiscoverLikeResultVO result = new DiscoverLikeResultVO();
            result.setSuccess(rpcResult.getSuccess());
            result.setIsLiked(rpcResult.getIsLiked());
            result.setLikeCount(rpcResult.getLikeCount());

            return result;
        } catch (Exception e) {
            log.error("切换点赞失败: likeDTO={}, error={}", likeDTO, e.getMessage(), e);
            DiscoverLikeResultVO result = new DiscoverLikeResultVO();
            result.setSuccess(false);
            return result;
        }
    }

    /**
     * 创建空结果
     */
    private DiscoverListResultVO createEmptyResult() {
        DiscoverListResultVO result = new DiscoverListResultVO();
        result.setList(Collections.emptyList());
        result.setHasMore(false);
        result.setTotal(0L);
        return result;
    }
}
