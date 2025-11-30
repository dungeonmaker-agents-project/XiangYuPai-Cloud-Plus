package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.HomeFeedResultVO;
import org.dromara.appbff.domain.vo.UserCardVO;
import org.dromara.appbff.service.HomeFeedService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.vo.LimitedTimePageResult;
import org.dromara.appuser.api.domain.vo.LimitedTimeUserVo;
import org.dromara.content.api.RemoteContentService;
import org.dromara.content.api.domain.vo.FeedSimpleVo;
import org.dromara.content.api.domain.vo.UserFeedsVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页用户推荐 Feed 服务实现 (RPC实现)
 *
 * <p>支持三种排序方式：</p>
 * <ul>
 *     <li>nearby: 附近用户（按距离由近到远）</li>
 *     <li>top_rated: 评分最高（按评分由高到低）</li>
 *     <li>latest: 最新上线（按在线状态优先，后续扩展为最后登录时间）</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeFeedServiceImpl implements HomeFeedService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @DubboReference
    private RemoteContentService remoteContentService;

    /**
     * 排序类型常量
     */
    private static final String SORT_NEARBY = "nearby";
    private static final String SORT_TOP_RATED = "top_rated";
    private static final String SORT_LATEST = "latest";

    /**
     * 每个用户获取的动态数量
     */
    private static final int FEED_LIMIT_PER_USER = 3;

    @Override
    public HomeFeedResultVO getHomeFeedList(HomeFeedQueryDTO queryDTO) {
        log.info("获取首页用户推荐列表 - type={}, sortBy={}, pageNum={}, pageSize={}, cityCode={}, lat={}, lng={}",
            queryDTO.getType(), queryDTO.getSortBy(), queryDTO.getPageNum(), queryDTO.getPageSize(),
            queryDTO.getCityCode(), queryDTO.getLatitude(), queryDTO.getLongitude());

        try {
            // 1. RPC调用用户服务，获取有技能的用户列表
            // 传入经纬度用于计算距离
            LimitedTimePageResult rpcResult = remoteAppUserService.queryLimitedTimeUsers(
                "all",  // gender - 首页不筛选性别，展示全部
                queryDTO.getCityCode(),
                queryDTO.getDistrictCode(),
                queryDTO.getLatitude(),   // 传入纬度用于距离计算
                queryDTO.getLongitude(),  // 传入经度用于距离计算
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
            );

            if (rpcResult == null || rpcResult.getList() == null || rpcResult.getList().isEmpty()) {
                log.warn("RPC调用返回空结果");
                return buildEmptyResult();
            }

            log.info("RPC调用成功，返回 {} 条用户数据", rpcResult.getList().size());

            // 2. 转换RPC结果为BFF VO
            List<UserCardVO> userList = rpcResult.getList().stream()
                .map(this::convertToUserCard)
                .collect(Collectors.toList());

            // 3. 批量获取用户动态 (RPC调用内容服务)
            enrichUserFeeds(userList);

            // 4. 应用排序（根据 sortBy 参数）
            String sortBy = queryDTO.getSortBy();
            List<UserCardVO> sortedUsers = applySorting(userList, sortBy);

            // 5. 构建响应
            return HomeFeedResultVO.builder()
                .list(sortedUsers)
                .total((long) rpcResult.getTotal())
                .hasMore(rpcResult.getHasMore())
                .build();

        } catch (Exception e) {
            log.error("获取首页用户推荐列表失败", e);
            return buildEmptyResult();
        }
    }

    /**
     * 批量获取用户动态并填充到用户卡片
     *
     * @param userList 用户列表
     */
    private void enrichUserFeeds(List<UserCardVO> userList) {
        if (userList == null || userList.isEmpty()) {
            return;
        }

        try {
            // 提取用户ID列表
            List<Long> userIds = userList.stream()
                .map(UserCardVO::getUserId)
                .collect(Collectors.toList());

            log.info("批量获取用户动态，用户数: {}", userIds.size());

            // RPC调用内容服务获取动态
            Map<Long, UserFeedsVo> userFeedsMap = remoteContentService.batchGetUserFeeds(userIds, FEED_LIMIT_PER_USER);

            if (userFeedsMap == null || userFeedsMap.isEmpty()) {
                log.warn("未获取到用户动态数据");
                return;
            }

            // 填充动态到用户卡片
            for (UserCardVO userCard : userList) {
                UserFeedsVo userFeeds = userFeedsMap.get(userCard.getUserId());
                if (userFeeds != null && userFeeds.getFeeds() != null) {
                    // 转换动态VO
                    List<UserCardVO.FeedPreviewVO> feedPreviews = userFeeds.getFeeds().stream()
                        .map(this::convertToFeedPreview)
                        .collect(Collectors.toList());
                    userCard.setFeeds(feedPreviews);
                    userCard.setFeedCount(userFeeds.getTotalCount());
                } else {
                    userCard.setFeeds(Collections.emptyList());
                }
            }

            log.info("用户动态填充完成");

        } catch (Exception e) {
            log.error("批量获取用户动态失败", e);
            // 失败时不影响主流程，仅设置空动态
            for (UserCardVO userCard : userList) {
                userCard.setFeeds(Collections.emptyList());
            }
        }
    }

    /**
     * 转换为动态预览VO
     */
    private UserCardVO.FeedPreviewVO convertToFeedPreview(FeedSimpleVo rpcFeed) {
        UserCardVO.FeedPreviewVO preview = new UserCardVO.FeedPreviewVO();
        preview.setFeedId(rpcFeed.getFeedId());
        preview.setCoverImage(rpcFeed.getCoverImage());
        preview.setContent(rpcFeed.getContent());
        preview.setLikeCount(rpcFeed.getLikeCount());
        preview.setCommentCount(rpcFeed.getCommentCount());
        return preview;
    }

    /**
     * 应用排序逻辑
     *
     * @param users  用户列表
     * @param sortBy 排序方式
     * @return 排序后的用户列表
     */
    private List<UserCardVO> applySorting(List<UserCardVO> users, String sortBy) {
        if (users == null || users.isEmpty()) {
            return users;
        }

        // 默认排序: nearby (附近用户)
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = SORT_NEARBY;
        }

        log.info("应用排序: {}", sortBy);

        switch (sortBy) {
            case SORT_NEARBY:
                // 按距离由近到远排序
                return users.stream()
                    .sorted(Comparator.comparingDouble(u -> u.getDistance() != null ? u.getDistance() : Double.MAX_VALUE))
                    .collect(Collectors.toList());

            case SORT_TOP_RATED:
                // 按评分由高到低排序
                return users.stream()
                    .sorted(Comparator.comparingDouble((UserCardVO u) ->
                        u.getRating() != null ? u.getRating() : 0.0).reversed())
                    .collect(Collectors.toList());

            case SORT_LATEST:
                // 按最后登录时间由新到旧排序
                // TODO: 目前 RPC 返回的数据没有 lastLoginTime，需要后端扩展
                // 暂时按在线状态排序（在线用户优先）
                return users.stream()
                    .sorted(Comparator.comparing((UserCardVO u) ->
                        u.getIsOnline() != null && u.getIsOnline() ? 0 : 1))
                    .collect(Collectors.toList());

            default:
                log.warn("未知的排序方式: {}, 使用默认排序(nearby)", sortBy);
                return users.stream()
                    .sorted(Comparator.comparingDouble(u -> u.getDistance() != null ? u.getDistance() : Double.MAX_VALUE))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 转换RPC VO为BFF UserCardVO
     */
    private UserCardVO convertToUserCard(LimitedTimeUserVo rpcUser) {
        UserCardVO userCard = new UserCardVO();

        // 基本信息
        userCard.setUserId(rpcUser.getUserId());
        userCard.setNickname(rpcUser.getNickname());
        userCard.setAvatar(rpcUser.getAvatar());
        userCard.setAge(rpcUser.getAge() != null ? rpcUser.getAge() : 0);
        userCard.setBio(rpcUser.getBio());
        userCard.setIsOnline(rpcUser.getIsOnline() != null ? rpcUser.getIsOnline() : false);

        // 性别转换: male/female -> 1/2
        if ("male".equals(rpcUser.getGender())) {
            userCard.setGender(1);
        } else if ("female".equals(rpcUser.getGender())) {
            userCard.setGender(2);
        } else {
            userCard.setGender(0); // 未知
        }

        // 距离信息
        if (rpcUser.getDistance() != null) {
            userCard.setDistance(rpcUser.getDistance().doubleValue());
            userCard.setDistanceText(formatDistance(rpcUser.getDistance()));
        } else {
            userCard.setDistance(0.0);
            userCard.setDistanceText("未知");
        }

        // 技能信息
        List<String> skills = new ArrayList<>();
        if (rpcUser.getSkillName() != null && !rpcUser.getSkillName().isEmpty()) {
            skills.add(rpcUser.getSkillName());
        }
        userCard.setSkills(skills);

        // 评分和订单数
        if (rpcUser.getRating() != null) {
            userCard.setRating(rpcUser.getRating().doubleValue());
        } else {
            userCard.setRating(0.0);
        }
        userCard.setOrderCount(rpcUser.getOrderCount() != null ? rpcUser.getOrderCount() : 0);

        // 统计信息
        userCard.setFansCount(rpcUser.getFansCount() != null ? rpcUser.getFansCount() : 0);
        userCard.setFeedCount(0); // 后续由内容服务填充

        // 是否已关注 (TODO: 后续根据当前登录用户查询)
        userCard.setIsFollowed(false);

        // 最后登录时间 (TODO: 需要后端扩展返回此字段)
        userCard.setLastLoginTime(null);

        // 动态列表 - 后续由 enrichUserFeeds 方法填充
        userCard.setFeeds(Collections.emptyList());

        return userCard;
    }

    /**
     * 格式化距离
     */
    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "m";
        } else {
            return String.format("%.1fkm", meters / 1000.0);
        }
    }

    /**
     * 构建空结果
     */
    private HomeFeedResultVO buildEmptyResult() {
        return HomeFeedResultVO.builder()
            .list(Collections.emptyList())
            .total(0L)
            .hasMore(false)
            .build();
    }
}
