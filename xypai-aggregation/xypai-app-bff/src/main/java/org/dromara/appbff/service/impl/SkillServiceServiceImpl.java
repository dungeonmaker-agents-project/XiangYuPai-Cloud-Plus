package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.ServiceListQueryDTO;
import org.dromara.appbff.domain.dto.ServiceReviewQueryDTO;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.SkillServiceService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.dto.SkillServiceQueryDto;
import org.dromara.appuser.api.domain.vo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能服务服务实现类（RPC版本）
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceServiceImpl implements SkillServiceService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @Override
    public ServiceListResultVO queryServiceList(ServiceListQueryDTO queryDTO, Long userId) {
        log.info("查询服务列表: queryDTO={}, userId={}", queryDTO, userId);

        try {
            // 1. 转换请求 DTO
            SkillServiceQueryDto rpcQuery = convertToRpcQuery(queryDTO, userId);

            // 2. 调用 RPC 接口
            SkillServicePageResult rpcResult = remoteAppUserService.querySkillServiceList(rpcQuery);

            // 3. 转换响应 VO
            return convertToListResult(rpcResult);
        } catch (Exception e) {
            log.error("查询服务列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            // 返回空结果作为降级处理
            return createEmptyListResult(queryDTO.getSkillType());
        }
    }

    @Override
    public ServiceDetailVO getServiceDetail(Long serviceId, Long userId) {
        log.info("获取服务详情: serviceId={}, userId={}", serviceId, userId);

        try {
            // 调用 RPC 接口
            SkillServiceDetailVo rpcResult = remoteAppUserService.getSkillServiceDetail(serviceId, userId);

            if (rpcResult == null) {
                log.warn("服务详情不存在: serviceId={}", serviceId);
                return null;
            }

            // 转换响应 VO
            return convertToDetailVO(rpcResult);
        } catch (Exception e) {
            log.error("获取服务详情失败: serviceId={}, error={}", serviceId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ServiceReviewListVO getServiceReviews(ServiceReviewQueryDTO queryDTO) {
        log.info("获取服务评价列表: queryDTO={}", queryDTO);

        try {
            // 调用 RPC 接口
            SkillServiceReviewPageResult rpcResult = remoteAppUserService.getSkillServiceReviews(
                queryDTO.getServiceId(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize(),
                queryDTO.getFilterBy()
            );

            if (rpcResult == null) {
                log.warn("服务评价列表不存在: serviceId={}", queryDTO.getServiceId());
                return createEmptyReviewList(queryDTO.getServiceId());
            }

            // 转换响应 VO
            return convertToReviewListVO(rpcResult);
        } catch (Exception e) {
            log.error("获取服务评价列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            return createEmptyReviewList(queryDTO.getServiceId());
        }
    }

    // ========== 转换方法 ==========

    /**
     * 转换BFF查询DTO为RPC查询DTO
     */
    private SkillServiceQueryDto convertToRpcQuery(ServiceListQueryDTO dto, Long userId) {
        SkillServiceQueryDto.SkillServiceQueryDtoBuilder builder = SkillServiceQueryDto.builder()
            .skillType(dto.getSkillType())
            .pageNum(dto.getPageNum() != null ? dto.getPageNum() : 1)
            .pageSize(dto.getPageSize() != null ? dto.getPageSize() : 10)
            .tabType(dto.getTabType())
            .sortBy(dto.getSortBy())
            .currentUserId(userId);

        // 转换筛选条件
        if (dto.getFilters() != null) {
            ServiceListQueryDTO.ServiceFilterDTO filters = dto.getFilters();
            builder.gender(filters.getGender())
                .status(filters.getStatus())
                .gameArea(filters.getGameArea())
                .ranks(filters.getRank())
                .priceRanges(filters.getPriceRange())
                .positions(filters.getPosition())
                .tags(filters.getTags())
                .cityCode(filters.getCityCode());
        }

        return builder.build();
    }

    /**
     * 转换RPC分页结果为BFF列表结果
     */
    private ServiceListResultVO convertToListResult(SkillServicePageResult rpc) {
        ServiceListResultVO result = new ServiceListResultVO();

        // 技能类型信息
        if (rpc.getSkillType() != null) {
            ServiceListResultVO.SkillTypeVO skillType = new ServiceListResultVO.SkillTypeVO();
            skillType.setType(rpc.getSkillType().getType());
            skillType.setLabel(rpc.getSkillType().getLabel());
            skillType.setIcon(rpc.getSkillType().getIcon());
            result.setSkillType(skillType);
        }

        // Tab列表
        if (rpc.getTabs() != null) {
            List<ServiceListResultVO.TabVO> tabs = rpc.getTabs().stream()
                .map(t -> {
                    ServiceListResultVO.TabVO tab = new ServiceListResultVO.TabVO();
                    tab.setValue(t.getValue());
                    tab.setLabel(t.getLabel());
                    tab.setCount(t.getCount());
                    return tab;
                })
                .collect(Collectors.toList());
            result.setTabs(tabs);
        }

        // 筛选配置
        if (rpc.getFilters() != null) {
            result.setFilters(convertFilterConfig(rpc.getFilters()));
        }

        // 数据列表
        if (rpc.getList() != null) {
            List<ServiceCardVO> cards = rpc.getList().stream()
                .map(this::convertToCard)
                .collect(Collectors.toList());
            result.setList(cards);
        } else {
            result.setList(Collections.emptyList());
        }

        result.setTotal(rpc.getTotal());
        result.setHasMore(rpc.getHasMore());

        return result;
    }

    /**
     * 转换筛选配置
     */
    private ServiceListResultVO.FilterConfigVO convertFilterConfig(SkillServicePageResult.FilterConfig rpcFilter) {
        ServiceListResultVO.FilterConfigVO config = new ServiceListResultVO.FilterConfigVO();

        // 转换排序选项
        if (rpcFilter.getSortOptions() != null) {
            config.setSortOptions(rpcFilter.getSortOptions().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        // 转换性别选项
        if (rpcFilter.getGenderOptions() != null) {
            config.setGenderOptions(rpcFilter.getGenderOptions().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        // 转换状态选项
        if (rpcFilter.getStatusOptions() != null) {
            config.setStatusOptions(rpcFilter.getStatusOptions().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        // 转换游戏大区
        if (rpcFilter.getGameAreas() != null) {
            config.setGameAreas(rpcFilter.getGameAreas().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        // 转换段位
        if (rpcFilter.getRanks() != null) {
            config.setRanks(rpcFilter.getRanks().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        // 转换价格区间
        if (rpcFilter.getPriceRanges() != null) {
            config.setPriceRanges(rpcFilter.getPriceRanges().stream()
                .map(p -> {
                    ServiceListResultVO.PriceRangeVO range = new ServiceListResultVO.PriceRangeVO();
                    range.setValue(p.getValue());
                    range.setLabel(p.getLabel());
                    range.setMin(p.getMin());
                    range.setMax(p.getMax());
                    return range;
                })
                .collect(Collectors.toList()));
        }

        // 转换位置
        if (rpcFilter.getPositions() != null) {
            config.setPositions(rpcFilter.getPositions().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        // 转换标签
        if (rpcFilter.getTags() != null) {
            config.setTags(rpcFilter.getTags().stream()
                .map(o -> convertOption(o))
                .collect(Collectors.toList()));
        }

        return config;
    }

    /**
     * 转换选项
     */
    private ServiceListResultVO.OptionVO convertOption(SkillServicePageResult.OptionInfo rpcOption) {
        ServiceListResultVO.OptionVO option = new ServiceListResultVO.OptionVO();
        option.setValue(rpcOption.getValue());
        option.setLabel(rpcOption.getLabel());
        return option;
    }

    /**
     * 转换技能服务VO为服务卡片VO
     */
    private ServiceCardVO convertToCard(SkillServiceVo rpc) {
        ServiceCardVO card = new ServiceCardVO();
        card.setServiceId(rpc.getSkillId());
        card.setDescription(rpc.getDescription());

        // Provider信息
        ServiceCardVO.ProviderBriefVO provider = new ServiceCardVO.ProviderBriefVO();
        provider.setUserId(rpc.getUserId());
        provider.setNickname(rpc.getNickname());
        provider.setAvatar(rpc.getAvatar());
        provider.setGender(rpc.getGender());
        provider.setAge(rpc.getAge());
        provider.setIsOnline(rpc.getIsOnline());
        provider.setIsVerified(rpc.getIsVerified());
        card.setProvider(provider);

        // Skill信息
        ServiceCardVO.SkillBriefVO skill = new ServiceCardVO.SkillBriefVO();
        skill.setSkillType(rpc.getSkillType());
        skill.setGameArea(rpc.getGameArea());
        skill.setRank(rpc.getRank());
        skill.setRankScore(rpc.getRankScore());
        skill.setPosition(rpc.getPositions());
        card.setSkillInfo(skill);

        // 标签
        if (rpc.getTags() != null) {
            card.setTags(rpc.getTags().stream()
                .map(t -> {
                    ServiceCardVO.TagVO tag = new ServiceCardVO.TagVO();
                    tag.setText(t.getText());
                    tag.setType(t.getType());
                    tag.setColor(t.getColor());
                    return tag;
                })
                .collect(Collectors.toList()));
        }

        // 价格
        ServiceCardVO.PriceVO price = new ServiceCardVO.PriceVO();
        price.setAmount(rpc.getPrice());
        price.setUnit(rpc.getPriceUnit());
        price.setDisplayText(rpc.getPriceDisplay());
        card.setPrice(price);

        // 统计
        ServiceCardVO.StatsVO stats = new ServiceCardVO.StatsVO();
        stats.setOrders(rpc.getOrderCount());
        stats.setRating(rpc.getRating());
        stats.setReviewCount(rpc.getReviewCount());
        card.setStats(stats);

        // 距离
        if (rpc.getDistance() != null) {
            card.setDistance(rpc.getDistance());
            card.setDistanceDisplay(formatDistance(rpc.getDistance()));
        }

        return card;
    }

    /**
     * 转换RPC详情VO为BFF详情VO
     */
    private ServiceDetailVO convertToDetailVO(SkillServiceDetailVo rpc) {
        ServiceDetailVO detail = new ServiceDetailVO();
        detail.setServiceId(rpc.getSkillId());
        detail.setBannerImage(rpc.getBannerImage());
        detail.setImages(rpc.getImages());
        detail.setDescription(rpc.getDescription());
        detail.setIsAvailable(rpc.getIsAvailable());
        detail.setUnavailableReason(rpc.getUnavailableReason());

        // Provider信息
        if (rpc.getProvider() != null) {
            ServiceDetailVO.ProviderDetailVO provider = new ServiceDetailVO.ProviderDetailVO();
            provider.setUserId(rpc.getProvider().getUserId());
            provider.setNickname(rpc.getProvider().getNickname());
            provider.setAvatar(rpc.getProvider().getAvatar());
            provider.setGender(rpc.getProvider().getGender());
            provider.setAge(rpc.getProvider().getAge());
            provider.setIsOnline(rpc.getProvider().getIsOnline());
            provider.setIsVerified(rpc.getProvider().getIsVerified());
            provider.setLevel(rpc.getProvider().getLevel());
            provider.setCertifications(rpc.getProvider().getCertifications());
            detail.setProvider(provider);
        }

        // Skill信息
        if (rpc.getSkillInfo() != null) {
            ServiceDetailVO.SkillDetailVO skill = new ServiceDetailVO.SkillDetailVO();
            skill.setSkillType(rpc.getSkillInfo().getSkillType());
            skill.setSkillLabel(rpc.getSkillInfo().getSkillLabel());
            skill.setGameArea(rpc.getSkillInfo().getGameArea());
            skill.setRank(rpc.getSkillInfo().getRank());
            skill.setRankScore(rpc.getSkillInfo().getRankScore());
            skill.setRankDisplay(rpc.getSkillInfo().getRankDisplay());
            skill.setPosition(rpc.getSkillInfo().getPosition());
            skill.setVoiceType(rpc.getSkillInfo().getVoiceType());
            detail.setSkillInfo(skill);
        }

        // 标签
        if (rpc.getTags() != null) {
            detail.setTags(rpc.getTags().stream()
                .map(t -> {
                    ServiceCardVO.TagVO tag = new ServiceCardVO.TagVO();
                    tag.setText(t.getText());
                    tag.setType(t.getType());
                    tag.setColor(t.getColor());
                    return tag;
                })
                .collect(Collectors.toList()));
        }

        // 价格
        if (rpc.getPrice() != null) {
            ServiceCardVO.PriceVO price = new ServiceCardVO.PriceVO();
            price.setAmount(rpc.getPrice().getAmount());
            price.setUnit(rpc.getPrice().getUnit());
            price.setDisplayText(rpc.getPrice().getDisplayText());
            detail.setPrice(price);
        }

        // 时间安排
        if (rpc.getSchedule() != null) {
            ServiceDetailVO.ScheduleVO schedule = new ServiceDetailVO.ScheduleVO();
            schedule.setAvailable(rpc.getSchedule().getAvailable());
            detail.setSchedule(schedule);
        }

        // 位置
        if (rpc.getLocation() != null) {
            ServiceDetailVO.LocationVO location = new ServiceDetailVO.LocationVO();
            location.setAddress(rpc.getLocation().getAddress());
            location.setDistrict(rpc.getLocation().getDistrict());
            location.setDistance(rpc.getLocation().getDistance());
            location.setDistanceDisplay(rpc.getLocation().getDistanceDisplay());
            detail.setLocation(location);
        }

        // 统计
        if (rpc.getStats() != null) {
            ServiceCardVO.StatsVO stats = new ServiceCardVO.StatsVO();
            stats.setOrders(rpc.getStats().getOrders());
            stats.setRating(rpc.getStats().getRating());
            stats.setReviewCount(rpc.getStats().getReviewCount());
            detail.setStats(stats);
        }

        // 评价信息
        if (rpc.getReviews() != null) {
            detail.setReviews(convertReviews(rpc.getReviews()));
        }

        return detail;
    }

    /**
     * 转换评价信息
     */
    private ServiceDetailVO.ReviewsVO convertReviews(SkillServiceDetailVo.ReviewsInfo rpcReviews) {
        ServiceDetailVO.ReviewsVO reviews = new ServiceDetailVO.ReviewsVO();
        reviews.setTotal(rpcReviews.getTotal());

        // 评价摘要
        if (rpcReviews.getSummary() != null) {
            ServiceDetailVO.ReviewSummaryVO summary = new ServiceDetailVO.ReviewSummaryVO();
            summary.setExcellent(rpcReviews.getSummary().getExcellent());
            summary.setPositive(rpcReviews.getSummary().getPositive());
            summary.setNegative(rpcReviews.getSummary().getNegative());
            reviews.setSummary(summary);
        }

        // 评价标签
        if (rpcReviews.getTags() != null) {
            reviews.setTags(rpcReviews.getTags().stream()
                .map(t -> {
                    ServiceDetailVO.ReviewTagVO tag = new ServiceDetailVO.ReviewTagVO();
                    tag.setText(t.getText());
                    tag.setCount(t.getCount());
                    return tag;
                })
                .collect(Collectors.toList()));
        }

        // 最近评价
        if (rpcReviews.getRecent() != null) {
            reviews.setRecent(rpcReviews.getRecent().stream()
                .map(r -> {
                    ServiceDetailVO.ReviewItemVO item = new ServiceDetailVO.ReviewItemVO();
                    item.setReviewId(r.getReviewId());
                    item.setRating(r.getRating());
                    item.setContent(r.getContent());
                    item.setCreatedAt(r.getCreatedAt());
                    item.setReply(r.getReply());

                    if (r.getReviewer() != null) {
                        ServiceDetailVO.ReviewerVO reviewer = new ServiceDetailVO.ReviewerVO();
                        reviewer.setUserId(r.getReviewer().getUserId());
                        reviewer.setNickname(r.getReviewer().getNickname());
                        reviewer.setAvatar(r.getReviewer().getAvatar());
                        item.setReviewer(reviewer);
                    }

                    return item;
                })
                .collect(Collectors.toList()));
        }

        return reviews;
    }

    /**
     * 转换评价列表结果
     */
    private ServiceReviewListVO convertToReviewListVO(SkillServiceReviewPageResult rpc) {
        ServiceReviewListVO result = new ServiceReviewListVO();
        result.setServiceId(rpc.getSkillId());

        // 评价摘要
        if (rpc.getSummary() != null) {
            ServiceDetailVO.ReviewSummaryVO summary = new ServiceDetailVO.ReviewSummaryVO();
            summary.setExcellent(rpc.getSummary().getExcellent());
            summary.setPositive(rpc.getSummary().getPositive());
            summary.setNegative(rpc.getSummary().getNegative());
            result.setSummary(summary);
        }

        // 评价标签
        if (rpc.getTags() != null) {
            result.setTags(rpc.getTags().stream()
                .map(t -> {
                    ServiceDetailVO.ReviewTagVO tag = new ServiceDetailVO.ReviewTagVO();
                    tag.setText(t.getText());
                    tag.setCount(t.getCount());
                    return tag;
                })
                .collect(Collectors.toList()));
        }

        // 评价列表
        if (rpc.getList() != null) {
            result.setList(rpc.getList().stream()
                .map(r -> {
                    ServiceDetailVO.ReviewItemVO item = new ServiceDetailVO.ReviewItemVO();
                    item.setReviewId(r.getReviewId());
                    item.setRating(r.getRating());
                    item.setContent(r.getContent());
                    item.setCreatedAt(r.getCreatedAt());
                    item.setReply(r.getReply());

                    if (r.getReviewer() != null) {
                        ServiceDetailVO.ReviewerVO reviewer = new ServiceDetailVO.ReviewerVO();
                        reviewer.setUserId(r.getReviewer().getUserId());
                        reviewer.setNickname(r.getReviewer().getNickname());
                        reviewer.setAvatar(r.getReviewer().getAvatar());
                        item.setReviewer(reviewer);
                    }

                    return item;
                })
                .collect(Collectors.toList()));
        } else {
            result.setList(Collections.emptyList());
        }

        result.setTotal(rpc.getTotal());
        result.setPageNum(rpc.getPageNum());
        result.setPageSize(rpc.getPageSize());
        result.setPages(rpc.getPages());
        result.setHasNext(rpc.getHasNext());

        return result;
    }

    // ========== 降级处理方法 ==========

    /**
     * 创建空的列表结果（降级处理）
     */
    private ServiceListResultVO createEmptyListResult(String skillType) {
        ServiceListResultVO result = new ServiceListResultVO();

        ServiceListResultVO.SkillTypeVO st = new ServiceListResultVO.SkillTypeVO();
        st.setType(skillType);
        st.setLabel(skillType);
        result.setSkillType(st);

        result.setTabs(Collections.emptyList());
        result.setFilters(new ServiceListResultVO.FilterConfigVO());
        result.setList(Collections.emptyList());
        result.setTotal(0L);
        result.setHasMore(false);

        return result;
    }

    /**
     * 创建空的评价列表（降级处理）
     */
    private ServiceReviewListVO createEmptyReviewList(Long serviceId) {
        ServiceReviewListVO result = new ServiceReviewListVO();
        result.setServiceId(serviceId);

        ServiceDetailVO.ReviewSummaryVO summary = new ServiceDetailVO.ReviewSummaryVO();
        summary.setExcellent(0);
        summary.setPositive(0);
        summary.setNegative(0);
        result.setSummary(summary);

        result.setTags(Collections.emptyList());
        result.setList(Collections.emptyList());
        result.setTotal(0L);
        result.setPageNum(1);
        result.setPageSize(10);
        result.setPages(0);
        result.setHasNext(false);

        return result;
    }

    /**
     * 格式化距离显示
     */
    private String formatDistance(Integer distance) {
        if (distance == null) return "";
        if (distance < 1000) {
            return distance + "m";
        }
        return String.format("%.1fkm", distance / 1000.0);
    }
}
