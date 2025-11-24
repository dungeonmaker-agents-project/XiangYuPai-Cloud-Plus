package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.LimitedTimeQueryDTO;
import org.dromara.appbff.domain.vo.LimitedTimeResultVO;
import org.dromara.appbff.domain.vo.LimitedTimeUserVO;
import org.dromara.appbff.service.HomeLimitedTimeService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.vo.LimitedTimePageResult;
import org.dromara.appuser.api.domain.vo.LimitedTimeUserVo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 限时专享服务实现 (RPC实现)
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeLimitedTimeServiceImpl implements HomeLimitedTimeService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    // 促销标签列表 (临时Mock，等促销服务就绪后替换为RPC调用)
    private static final String[] PROMOTION_TAGS = {
        "限时特价", "今日优惠", "新人专享", "热门推荐", "超值特惠"
    };

    @Override
    public LimitedTimeResultVO getLimitedTimeList(LimitedTimeQueryDTO queryDTO) {
        log.info("获取限时专享列表 - 查询参数: {}", queryDTO);

        // 1. RPC调用用户服务，获取有技能的用户列表
        LimitedTimePageResult rpcResult = remoteAppUserService.queryLimitedTimeUsers(
            queryDTO.getGender(),
            queryDTO.getCityCode(),
            queryDTO.getDistrictCode(),
            null, // latitude - 后续可从当前登录用户获取
            null, // longitude
            queryDTO.getPageNum(),
            queryDTO.getPageSize()
        );

        if (rpcResult == null || rpcResult.getList() == null || rpcResult.getList().isEmpty()) {
            log.warn("RPC调用返回空结果");
            return buildEmptyResult();
        }

        // 2. 转换RPC结果为BFF VO
        List<LimitedTimeUserVO> limitedTimeUsers = rpcResult.getList().stream()
            .map(this::convertToLimitedTimeUser)
            .collect(Collectors.toList());

        // 3. 应用排序 (在数据库已经按在线状态排序了，这里根据sortBy再排序)
        List<LimitedTimeUserVO> sortedUsers = applySorting(limitedTimeUsers, queryDTO.getSortBy());

        // 4. 构建响应
        return LimitedTimeResultVO.builder()
            .total(rpcResult.getTotal())
            .hasMore(rpcResult.getHasMore())
            .filters(buildFilterOptions())
            .list(sortedUsers)
            .build();
    }

    /**
     * 转换RPC VO为BFF VO
     */
    private LimitedTimeUserVO convertToLimitedTimeUser(LimitedTimeUserVo rpcUser) {
        // 计算促销价格 (临时Mock 7折优惠，等促销服务就绪后替换)
        int originalPrice = rpcUser.getPrice() != null ? rpcUser.getPrice().intValue() : 15;
        int promotionPrice = (int) (originalPrice * 0.7);

        // 选择促销标签 (轮流使用)
        String promotionTag = PROMOTION_TAGS[(rpcUser.getUserId().intValue()) % PROMOTION_TAGS.length];

        // 构建标签列表
        List<LimitedTimeUserVO.UserTag> tags = new ArrayList<>();

        // 在线/线下标签
        if (rpcUser.getIsOnline() != null && rpcUser.getIsOnline()) {
            tags.add(LimitedTimeUserVO.UserTag.builder()
                .text("可线上")
                .type("feature")
                .color("#4CAF50")
                .build());
        }

        // 促销标签
        tags.add(LimitedTimeUserVO.UserTag.builder()
            .text("限时7折")
            .type("price")
            .color("#FF5722")
            .build());

        // 技能标签
        if (rpcUser.getSkillName() != null) {
            tags.add(LimitedTimeUserVO.UserTag.builder()
                .text(rpcUser.getSkillName())
                .type("skill")
                .color("#2196F3")
                .build());
        }

        return LimitedTimeUserVO.builder()
            .userId(rpcUser.getUserId())
            .avatar(rpcUser.getAvatar())
            .nickname(rpcUser.getNickname())
            .gender(rpcUser.getGender())
            .age(rpcUser.getAge() != null ? rpcUser.getAge() : 0)
            .distance(rpcUser.getDistance() != null ? rpcUser.getDistance() : 0)
            .distanceText(formatDistance(rpcUser.getDistance() != null ? rpcUser.getDistance() : 0))
            .tags(tags)
            .description(rpcUser.getBio() != null && !rpcUser.getBio().isEmpty() ?
                rpcUser.getBio() : "资深陪玩师，服务态度好，技术过硬，限时特价优惠中！")
            .price(LimitedTimeUserVO.PriceInfo.builder()
                .amount(promotionPrice)
                .unit(rpcUser.getPriceUnit() != null ? rpcUser.getPriceUnit() : "per_order")
                .displayText(promotionPrice + " 金币/" +
                    (rpcUser.getPriceUnit() != null && rpcUser.getPriceUnit().equals("小时") ? "小时" : "单"))
                .originalPrice(originalPrice)
                .build())
            .promotionTag(promotionTag)
            .isOnline(rpcUser.getIsOnline() != null ? rpcUser.getIsOnline() : false)
            .skillLevel(rpcUser.getSkillLevel() != null ? rpcUser.getSkillLevel() : "熟练")
            .build();
    }

    /**
     * 应用排序
     */
    private List<LimitedTimeUserVO> applySorting(List<LimitedTimeUserVO> users, String sortBy) {
        if (sortBy == null || "smart_recommend".equals(sortBy)) {
            // 智能推荐排序 (综合评分：在线优先 + 距离近优先 + 价格低优先)
            return users.stream()
                .sorted(Comparator.comparingInt(u ->
                    -(u.getIsOnline() ? 1000 : 0) // 在线用户优先
                        - (1000 - u.getDistance() / 100) // 距离近优先
                        - (100 - u.getPrice().getAmount()))) // 价格低优先
                .collect(Collectors.toList());
        } else if ("price_asc".equals(sortBy)) {
            // 价格从低到高
            return users.stream()
                .sorted(Comparator.comparingInt(u -> u.getPrice().getAmount()))
                .collect(Collectors.toList());
        } else if ("price_desc".equals(sortBy)) {
            // 价格从高到低
            return users.stream()
                .sorted(Comparator.comparingInt(u -> -u.getPrice().getAmount()))
                .collect(Collectors.toList());
        } else if ("distance_asc".equals(sortBy)) {
            // 距离最近
            return users.stream()
                .sorted(Comparator.comparingInt(LimitedTimeUserVO::getDistance))
                .collect(Collectors.toList());
        }

        return users;
    }

    /**
     * 构建筛选选项
     */
    private LimitedTimeResultVO.FilterOptions buildFilterOptions() {
        return LimitedTimeResultVO.FilterOptions.builder()
            .sortOptions(Arrays.asList(
                LimitedTimeResultVO.Option.builder().value("smart_recommend").label("智能推荐").build(),
                LimitedTimeResultVO.Option.builder().value("price_asc").label("价格从低到高").build(),
                LimitedTimeResultVO.Option.builder().value("price_desc").label("价格从高到低").build(),
                LimitedTimeResultVO.Option.builder().value("distance_asc").label("距离最近").build()
            ))
            .genderOptions(Arrays.asList(
                LimitedTimeResultVO.Option.builder().value("all").label("不限性别").build(),
                LimitedTimeResultVO.Option.builder().value("male").label("男").build(),
                LimitedTimeResultVO.Option.builder().value("female").label("女").build()
            ))
            .languageOptions(Arrays.asList(
                LimitedTimeResultVO.Option.builder().value("all").label("语言(不限)").build(),
                LimitedTimeResultVO.Option.builder().value("mandarin").label("普通话").build(),
                LimitedTimeResultVO.Option.builder().value("cantonese").label("粤语").build(),
                LimitedTimeResultVO.Option.builder().value("english").label("英语").build()
            ))
            .build();
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
    private LimitedTimeResultVO buildEmptyResult() {
        return LimitedTimeResultVO.builder()
            .total(0)
            .hasMore(false)
            .filters(buildFilterOptions())
            .list(Collections.emptyList())
            .build();
    }
}
