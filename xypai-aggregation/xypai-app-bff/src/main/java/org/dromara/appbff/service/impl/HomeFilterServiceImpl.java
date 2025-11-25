package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.FilterApplyDTO;
import org.dromara.appbff.domain.vo.FilterConfigVO;
import org.dromara.appbff.domain.vo.FilterResultVO;
import org.dromara.appbff.domain.vo.UserCardVO;
import org.dromara.appbff.service.HomeFilterService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.dto.FilterQueryDto;
import org.dromara.appuser.api.domain.vo.FilterConfigVo;
import org.dromara.appuser.api.domain.vo.FilterUserPageResult;
import org.dromara.appuser.api.domain.vo.FilterUserVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页筛选服务实现 (RPC实现)
 * <p>
 * 通过 Dubbo RPC 调用 xypai-user 服务获取真实数据
 *
 * @author XyPai Team
 * @date 2025-11-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeFilterServiceImpl implements HomeFilterService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @Override
    public FilterConfigVO getFilterConfig(String type) {
        log.info("【筛选配置】获取筛选配置, type: {}", type);

        boolean isOnline = "online".equalsIgnoreCase(type);

        // 1. RPC调用获取数据库中的筛选配置
        FilterConfigVo rpcConfig = remoteAppUserService.getFilterConfig(type);

        // 2. 构建BFF响应
        FilterConfigVO config = FilterConfigVO.builder()
            // 年龄范围 (固定配置)
            .ageRange(FilterConfigVO.AgeRange.builder()
                .min(18)
                .max(null) // null 表示不限
                .build())
            // 性别选项 (固定配置)
            .genderOptions(Arrays.asList(
                FilterConfigVO.Option.builder().value("all").label("全部").build(),
                FilterConfigVO.Option.builder().value("male").label("男").build(),
                FilterConfigVO.Option.builder().value("female").label("女").build()
            ))
            // 状态选项 (固定配置)
            .statusOptions(Arrays.asList(
                FilterConfigVO.Option.builder().value("online").label("在线").build(),
                FilterConfigVO.Option.builder().value("active_3d").label("近三天活跃").build(),
                FilterConfigVO.Option.builder().value("active_7d").label("近七天活跃").build()
            ))
            // 标签选项 (固定配置)
            .tagOptions(Arrays.asList(
                FilterConfigVO.TagOption.builder().value("荣耀王者").label("荣耀王者").highlighted(true).build(),
                FilterConfigVO.TagOption.builder().value("大神认证").label("大神认证").highlighted(true).build(),
                FilterConfigVO.TagOption.builder().value("巅峰赛").label("巅峰赛").highlighted(false).build(),
                FilterConfigVO.TagOption.builder().value("带粉上分").label("带粉上分").highlighted(false).build(),
                FilterConfigVO.TagOption.builder().value("官方认证").label("官方认证").highlighted(true).build(),
                FilterConfigVO.TagOption.builder().value("声优陪玩").label("声优陪玩").highlighted(false).build()
            ))
            .build();

        // 3. 从RPC结果构建技能选项 (来自数据库)
        if (rpcConfig != null && rpcConfig.getSkillOptions() != null) {
            List<FilterConfigVO.SkillOption> skillOptions = rpcConfig.getSkillOptions().stream()
                .map(opt -> FilterConfigVO.SkillOption.builder()
                    .value(opt.getValue())
                    .label(opt.getLabel() + " (" + opt.getCount() + "人)")
                    .category(opt.getCategory())
                    .build())
                .collect(Collectors.toList());
            config.setSkillOptions(skillOptions);
        } else {
            // 如果RPC返回空，使用默认配置
            if (isOnline) {
                config.setSkillOptions(Arrays.asList(
                    FilterConfigVO.SkillOption.builder().value("荣耀王者").label("荣耀王者").category("王者荣耀").build(),
                    FilterConfigVO.SkillOption.builder().value("王者").label("最强王者").category("王者荣耀").build(),
                    FilterConfigVO.SkillOption.builder().value("星耀").label("至尊星耀").category("王者荣耀").build(),
                    FilterConfigVO.SkillOption.builder().value("钻石").label("永恒钻石").category("王者荣耀").build()
                ));
            } else {
                config.setSkillOptions(Arrays.asList(
                    FilterConfigVO.SkillOption.builder().value("健身教练").label("健身教练").category("运动健身").build(),
                    FilterConfigVO.SkillOption.builder().value("瑜伽老师").label("瑜伽老师").category("运动健身").build()
                ));
            }
        }

        // 4. 价格选项 (线上模式特有)
        if (isOnline) {
            // 使用RPC返回的价格范围，或者默认值
            int minPrice = 4;
            int maxPrice = 30;
            if (rpcConfig != null && rpcConfig.getPriceRange() != null) {
                minPrice = rpcConfig.getPriceRange().getMinPrice() != null ?
                    rpcConfig.getPriceRange().getMinPrice() : 4;
                maxPrice = rpcConfig.getPriceRange().getMaxPrice() != null ?
                    rpcConfig.getPriceRange().getMaxPrice() : 30;
            }

            config.setPriceOptions(Arrays.asList(
                FilterConfigVO.PriceOption.builder()
                    .value(minPrice + "-" + Math.min(minPrice + 5, maxPrice))
                    .label(minPrice + "~" + Math.min(minPrice + 5, maxPrice) + "币")
                    .min(minPrice).max(Math.min(minPrice + 5, maxPrice)).build(),
                FilterConfigVO.PriceOption.builder()
                    .value("10-19").label("10~19币").min(10).max(19).build(),
                FilterConfigVO.PriceOption.builder()
                    .value("20+").label("20币以上").min(20).max(null).build()
            ));

            config.setPositionOptions(Arrays.asList(
                FilterConfigVO.Option.builder().value("打野").label("打野").build(),
                FilterConfigVO.Option.builder().value("上路").label("上路").build(),
                FilterConfigVO.Option.builder().value("中路").label("中路").build(),
                FilterConfigVO.Option.builder().value("下路").label("下路").build(),
                FilterConfigVO.Option.builder().value("辅助").label("辅助").build()
            ));
        } else {
            // 线下模式：不设置价格和位置选项
            config.setPriceOptions(null);
            config.setPositionOptions(null);
        }

        log.info("【筛选配置】筛选配置返回成功, 技能选项数: {}",
            config.getSkillOptions() != null ? config.getSkillOptions().size() : 0);
        return config;
    }

    @Override
    public FilterResultVO applyFilter(FilterApplyDTO dto) {
        log.info("【筛选应用】应用筛选条件, type: {}, pageNum: {}, pageSize: {}",
            dto.getType(), dto.getPageNum(), dto.getPageSize());

        // 1. 构建RPC查询DTO
        FilterQueryDto queryDto = FilterQueryDto.builder()
            .type(dto.getType())
            .pageNum(dto.getPageNum())
            .pageSize(dto.getPageSize())
            .build();

        // 2. 解析筛选条件
        List<String> summary = new ArrayList<>();
        int filterCount = 0;

        FilterApplyDTO.FilterCriteria filters = dto.getFilters();
        if (filters != null) {
            // 年龄
            if (filters.getAge() != null) {
                queryDto.setAgeMin(filters.getAge().getMin());
                queryDto.setAgeMax(filters.getAge().getMax());
                filterCount++;
                String ageDesc = filters.getAge().getMin() + "岁";
                if (filters.getAge().getMax() != null) {
                    ageDesc += " - " + filters.getAge().getMax() + "岁";
                } else {
                    ageDesc += " - 不限";
                }
                summary.add("年龄: " + ageDesc);
            }

            // 性别
            if (filters.getGender() != null && !"all".equals(filters.getGender())) {
                queryDto.setGender(filters.getGender());
                filterCount++;
                String genderLabel = "male".equals(filters.getGender()) ? "男" : "女";
                summary.add("性别: " + genderLabel);
            }

            // 状态
            if (filters.getStatus() != null) {
                queryDto.setStatus(filters.getStatus());
                filterCount++;
                String statusLabel = switch (filters.getStatus()) {
                    case "online" -> "在线";
                    case "active_3d" -> "近三天活跃";
                    case "active_7d" -> "近七天活跃";
                    default -> filters.getStatus();
                };
                summary.add("状态: " + statusLabel);
            }

            // 技能
            if (filters.getSkills() != null && !filters.getSkills().isEmpty()) {
                queryDto.setSkills(filters.getSkills());
                filterCount++;
                summary.add("技能: " + String.join(", ", filters.getSkills()));
            }

            // 价格
            if (filters.getPriceRange() != null && !filters.getPriceRange().isEmpty()) {
                queryDto.setPriceRanges(filters.getPriceRange());
                filterCount++;
                summary.add("价格: " + String.join(", ", filters.getPriceRange()));
            }

            // 位置 (暂不传递给RPC，后续可扩展)
            if (filters.getPositions() != null && !filters.getPositions().isEmpty()) {
                filterCount++;
                summary.add("位置: " + String.join(", ", filters.getPositions()));
            }

            // 标签 (暂不传递给RPC，后续可扩展)
            if (filters.getTags() != null && !filters.getTags().isEmpty()) {
                queryDto.setSkills(filters.getTags()); // 暂时用tags作为技能筛选
                filterCount++;
                summary.add("标签: " + String.join(", ", filters.getTags()));
            }
        }

        // 3. RPC调用获取用户列表
        FilterUserPageResult rpcResult = remoteAppUserService.queryFilteredUsers(queryDto);

        // 4. 转换RPC结果为BFF VO
        List<UserCardVO> users;
        long total = 0;
        boolean hasMore = false;

        if (rpcResult != null && rpcResult.getList() != null && !rpcResult.getList().isEmpty()) {
            users = rpcResult.getList().stream()
                .map(this::convertToUserCard)
                .collect(Collectors.toList());
            total = rpcResult.getTotal() != null ? rpcResult.getTotal() : 0;
            hasMore = rpcResult.getHasMore() != null ? rpcResult.getHasMore() : false;
            log.info("【筛选应用】RPC返回用户数: {}, 总数: {}", users.size(), total);
        } else {
            users = Collections.emptyList();
            log.warn("【筛选应用】RPC返回空结果");
        }

        // 5. 构建响应
        FilterResultVO result = FilterResultVO.builder()
            .total(total)
            .hasMore(hasMore)
            .list(users)
            .appliedFilters(FilterResultVO.AppliedFilters.builder()
                .count(filterCount)
                .summary(summary)
                .build())
            .build();

        log.info("【筛选应用】筛选结果返回成功, 用户数: {}, 筛选条件数: {}", users.size(), filterCount);
        return result;
    }

    /**
     * 转换RPC VO为BFF UserCardVO
     */
    private UserCardVO convertToUserCard(FilterUserVo rpcUser) {
        UserCardVO user = new UserCardVO();
        user.setUserId(rpcUser.getUserId());
        user.setNickname(rpcUser.getNickname());
        user.setAvatar(rpcUser.getAvatar());

        // 性别转换: male/female -> 1/2
        if ("male".equals(rpcUser.getGender())) {
            user.setGender(1);
        } else if ("female".equals(rpcUser.getGender())) {
            user.setGender(2);
        } else {
            user.setGender(0);
        }

        user.setAge(rpcUser.getAge() != null ? rpcUser.getAge() : 0);
        user.setIsOnline(rpcUser.getIsOnline() != null ? rpcUser.getIsOnline() : false);
        user.setBio(rpcUser.getBio());

        // 技能信息
        List<String> skills = new ArrayList<>();
        if (rpcUser.getSkillName() != null) {
            String skillDisplay = rpcUser.getSkillName();
            if (rpcUser.getSkillLevel() != null) {
                skillDisplay += "-" + rpcUser.getSkillLevel();
            }
            skills.add(skillDisplay);
        }
        user.setSkills(skills);

        // 距离
        if (rpcUser.getDistance() != null && rpcUser.getDistance() > 0) {
            user.setDistance((double) rpcUser.getDistance());
            user.setDistanceText(formatDistance(rpcUser.getDistance()));
        }

        // 城市 (从residence解析)
        if (rpcUser.getResidence() != null) {
            user.setCity(rpcUser.getResidence());
        }

        // 统计信息
        user.setFansCount(rpcUser.getFansCount() != null ? rpcUser.getFansCount() : 0);
        user.setFeedCount(rpcUser.getPostsCount() != null ? rpcUser.getPostsCount() : 0);
        user.setIsFollowed(false); // 需要后续查询关注关系

        return user;
    }

    /**
     * 格式化距离文本
     */
    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "m";
        } else {
            return String.format("%.1fkm", meters / 1000.0);
        }
    }
}
