package org.dromara.content.dubbo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.content.api.RemoteDiscoverService;
import org.dromara.content.api.domain.dto.RemoteDiscoverLikeDto;
import org.dromara.content.api.domain.dto.RemoteDiscoverQueryDto;
import org.dromara.content.api.domain.vo.RemoteDiscoverFeedVo;
import org.dromara.content.api.domain.vo.RemoteDiscoverLikeResultVo;
import org.dromara.content.api.domain.vo.RemoteDiscoverPageResult;
import org.dromara.content.domain.dto.DiscoverListQueryDTO;
import org.dromara.content.domain.vo.DiscoverFeedVO;
import org.dromara.content.service.IDiscoverService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发现页远程服务实现
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteDiscoverServiceImpl implements RemoteDiscoverService {

    private final IDiscoverService discoverService;

    @Override
    public RemoteDiscoverPageResult queryDiscoverList(RemoteDiscoverQueryDto queryDTO, Long userId) {
        log.info("RPC查询发现列表: tab={}, pageNum={}, pageSize={}, userId={}",
            queryDTO.getTab(), queryDTO.getPageNum(), queryDTO.getPageSize(), userId);

        try {
            // 转换查询DTO
            DiscoverListQueryDTO localQuery = new DiscoverListQueryDTO();
            localQuery.setTab(queryDTO.getTab());
            localQuery.setPageNum(queryDTO.getPageNum());
            localQuery.setPageSize(queryDTO.getPageSize());
            localQuery.setLatitude(queryDTO.getLatitude());
            localQuery.setLongitude(queryDTO.getLongitude());
            localQuery.setCityId(queryDTO.getCityId());

            // 调用本地服务
            Page<DiscoverFeedVO> page = discoverService.queryDiscoverList(localQuery, userId);

            // 转换结果
            RemoteDiscoverPageResult result = new RemoteDiscoverPageResult();
            result.setList(page.getRecords().stream()
                .map(this::convertToRemoteVo)
                .collect(Collectors.toList()));
            result.setTotal(page.getTotal());
            result.setPageNum((int) page.getCurrent());
            result.setPageSize((int) page.getSize());
            result.setPages((int) page.getPages());
            result.setHasMore(page.getCurrent() < page.getPages());

            log.info("RPC查询发现列表成功: total={}, pageNum={}, hasMore={}",
                result.getTotal(), result.getPageNum(), result.getHasMore());

            return result;
        } catch (Exception e) {
            log.error("RPC查询发现列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            // 返回空结果
            RemoteDiscoverPageResult emptyResult = new RemoteDiscoverPageResult();
            emptyResult.setList(Collections.emptyList());
            emptyResult.setTotal(0L);
            emptyResult.setPageNum(1);
            emptyResult.setPageSize(20);
            emptyResult.setPages(0);
            emptyResult.setHasMore(false);
            return emptyResult;
        }
    }

    @Override
    public RemoteDiscoverLikeResultVo toggleLike(RemoteDiscoverLikeDto likeDto, Long userId) {
        log.info("RPC切换点赞: contentId={}, action={}, userId={}",
            likeDto.getContentId(), likeDto.getAction(), userId);

        try {
            boolean isLike = likeDto.isLikeAction();
            Integer newLikeCount = discoverService.toggleLike(likeDto.getContentId(), userId, isLike);

            return RemoteDiscoverLikeResultVo.success(isLike, newLikeCount);
        } catch (Exception e) {
            log.error("RPC切换点赞失败: likeDto={}, error={}", likeDto, e.getMessage(), e);
            return RemoteDiscoverLikeResultVo.fail(e.getMessage());
        }
    }

    @Override
    public Map<Long, Boolean> batchCheckLikeStatus(List<Long> feedIds, Long userId) {
        log.info("RPC批量检查点赞状态: feedIds.size={}, userId={}", feedIds.size(), userId);

        try {
            return discoverService.batchCheckLikeStatus(feedIds, userId);
        } catch (Exception e) {
            log.error("RPC批量检查点赞状态失败: error={}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 转换为远程VO
     */
    private RemoteDiscoverFeedVo convertToRemoteVo(DiscoverFeedVO local) {
        RemoteDiscoverFeedVo remote = new RemoteDiscoverFeedVo();
        remote.setId(local.getId());
        remote.setType(local.getType());
        remote.setCoverUrl(local.getCoverUrl());
        remote.setAspectRatio(local.getAspectRatio());
        remote.setDuration(local.getDuration());
        remote.setWidth(local.getWidth());
        remote.setHeight(local.getHeight());
        remote.setTitle(local.getTitle());
        remote.setContent(local.getContent());
        remote.setUserId(local.getUserId());
        remote.setUserAvatar(local.getUserAvatar());
        remote.setUserNickname(local.getUserNickname());
        remote.setLikeCount(local.getLikeCount());
        remote.setIsLiked(local.getIsLiked());
        remote.setCommentCount(local.getCommentCount());
        remote.setCollectCount(local.getCollectCount());
        remote.setIsCollected(local.getIsCollected());
        remote.setCreateTime(local.getCreateTime());
        remote.setLocation(local.getLocation());
        remote.setDistance(local.getDistance());
        return remote;
    }
}
