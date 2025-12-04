package org.dromara.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.domain.entity.*;
import org.dromara.user.domain.dto.SkillCreateDto;
import org.dromara.user.domain.dto.SkillUpdateDto;
import org.dromara.user.domain.dto.OnlineSkillCreateDto;
import org.dromara.user.domain.dto.OfflineSkillCreateDto;
import org.dromara.user.domain.dto.AvailableTimeDto;
import org.dromara.user.domain.dto.SkilledUsersQueryDto;
import org.dromara.user.domain.vo.*;
import org.dromara.user.mapper.*;
import org.dromara.user.service.ISkillService;
import org.dromara.user.service.ISkillConfigService;
import org.dromara.appuser.api.domain.vo.LimitedTimeUserVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能服务实现
 * Skill Service Implementation
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Service
@RequiredArgsConstructor
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill> implements ISkillService {

    private final SkillMapper skillMapper;
    private final SkillImageMapper skillImageMapper;
    private final SkillPromiseMapper skillPromiseMapper;
    private final SkillAvailableTimeMapper skillAvailableTimeMapper;
    private final UserMapper userMapper;
    private final ISkillConfigService skillConfigService;

    // 促销标签列表
    private static final String[] PROMOTION_TAGS = {
        "限时特价", "今日优惠", "新人专享", "热门推荐", "超值特惠"
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createSkill(Long userId, SkillCreateDto dto) {
        // Create skill
        Skill skill = Skill.builder()
            .userId(userId)
            .skillName(dto.getSkillName())
            .skillType(dto.getSkillType())
            .coverImage(dto.getCoverImage())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .priceUnit(dto.getPriceUnit())
            .isOnline(false) // Default to offline
            .rating(BigDecimal.ZERO)
            .reviewCount(0)
            .orderCount(0)
            .gameName(dto.getGameName())
            .gameRank(dto.getGameRank())
            .serviceHours(dto.getServiceHours())
            .serviceType(dto.getServiceType())
            .serviceLocation(dto.getServiceLocation())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .build();

        skillMapper.insert(skill);

        // Create skill images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 1;
            for (String imageUrl : dto.getImages()) {
                SkillImage image = SkillImage.builder()
                    .skillId(skill.getSkillId())
                    .imageUrl(imageUrl)
                    .sortOrder(order++)
                    .build();
                skillImageMapper.insert(image);
            }
        }

        // Create skill promises
        if (dto.getPromises() != null && !dto.getPromises().isEmpty()) {
            int order = 1;
            for (String promiseText : dto.getPromises()) {
                SkillPromise promise = SkillPromise.builder()
                    .skillId(skill.getSkillId())
                    .promiseText(promiseText)
                    .sortOrder(order++)
                    .build();
                skillPromiseMapper.insert(promise);
            }
        }

        return R.ok(skill.getSkillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createOnlineSkill(Long userId, OnlineSkillCreateDto dto) {
        // 校验 skillConfigId 有效性
        if (dto.getSkillConfigId() != null && !dto.getSkillConfigId().isEmpty()) {
            Long configId = Long.parseLong(dto.getSkillConfigId());
            if (!skillConfigService.isValidSkillConfigId(configId)) {
                return R.fail("无效的技能配置ID");
            }
            if (!skillConfigService.isSkillTypeMatch(configId, "online")) {
                return R.fail("技能配置与技能类型不匹配");
            }
        }

        // Create online skill
        Skill skill = Skill.builder()
            .userId(userId)
            .skillName(dto.getSkillName())
            .skillType("online")
            .coverImage(dto.getCoverImage())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .priceUnit("局")
            .isOnline(dto.getIsOnline() != null ? dto.getIsOnline() : false)
            .rating(BigDecimal.ZERO)
            .reviewCount(0)
            .orderCount(0)
            .gameName(dto.getGameName())
            .gameRank(dto.getGameRank())
            .serviceHours(dto.getServiceHours())
            .build();

        skillMapper.insert(skill);

        // Create skill images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 1;
            for (String imageUrl : dto.getImages()) {
                SkillImage image = SkillImage.builder()
                    .skillId(skill.getSkillId())
                    .imageUrl(imageUrl)
                    .sortOrder(order++)
                    .build();
                skillImageMapper.insert(image);
            }
        }

        // Create skill promises
        if (dto.getPromises() != null && !dto.getPromises().isEmpty()) {
            int order = 1;
            for (String promiseText : dto.getPromises()) {
                SkillPromise promise = SkillPromise.builder()
                    .skillId(skill.getSkillId())
                    .promiseText(promiseText)
                    .sortOrder(order++)
                    .build();
                skillPromiseMapper.insert(promise);
            }
        }

        return R.ok(skill.getSkillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createOfflineSkill(Long userId, OfflineSkillCreateDto dto) {
        // 校验 skillConfigId 有效性
        if (dto.getSkillConfigId() != null && !dto.getSkillConfigId().isEmpty()) {
            Long configId = Long.parseLong(dto.getSkillConfigId());
            if (!skillConfigService.isValidSkillConfigId(configId)) {
                return R.fail("无效的技能配置ID");
            }
            if (!skillConfigService.isSkillTypeMatch(configId, "offline")) {
                return R.fail("技能配置与技能类型不匹配");
            }
        }

        // Create offline skill
        Skill skill = Skill.builder()
            .userId(userId)
            .skillName(dto.getSkillName())
            .skillType("offline")
            .coverImage(dto.getCoverImage())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .priceUnit("小时")
            .isOnline(dto.getIsOnline() != null ? dto.getIsOnline() : false)
            .rating(BigDecimal.ZERO)
            .reviewCount(0)
            .orderCount(0)
            .serviceType(dto.getServiceType())
            .serviceLocation(dto.getLocation().getAddress())
            .latitude(dto.getLocation().getLatitude())
            .longitude(dto.getLocation().getLongitude())
            .build();

        skillMapper.insert(skill);

        // Create skill images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int order = 1;
            for (String imageUrl : dto.getImages()) {
                SkillImage image = SkillImage.builder()
                    .skillId(skill.getSkillId())
                    .imageUrl(imageUrl)
                    .sortOrder(order++)
                    .build();
                skillImageMapper.insert(image);
            }
        }

        // Create skill promises
        if (dto.getPromises() != null && !dto.getPromises().isEmpty()) {
            int order = 1;
            for (String promiseText : dto.getPromises()) {
                SkillPromise promise = SkillPromise.builder()
                    .skillId(skill.getSkillId())
                    .promiseText(promiseText)
                    .sortOrder(order++)
                    .build();
                skillPromiseMapper.insert(promise);
            }
        }

        // Create available times
        if (dto.getAvailableTimes() != null && !dto.getAvailableTimes().isEmpty()) {
            for (AvailableTimeDto timeDto : dto.getAvailableTimes()) {
                SkillAvailableTime availableTime = SkillAvailableTime.builder()
                    .skillId(skill.getSkillId())
                    .dayOfWeek(timeDto.getDayOfWeek())
                    .startTime(timeDto.getStartTime())
                    .endTime(timeDto.getEndTime())
                    .build();
                skillAvailableTimeMapper.insert(availableTime);
            }
        }

        return R.ok(skill.getSkillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateSkill(Long userId, Long skillId, SkillUpdateDto dto) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            return R.fail("Skill not found");
        }

        if (!skill.getUserId().equals(userId)) {
            return R.fail("Not authorized");
        }

        // Update skill fields
        if (dto.getSkillName() != null) skill.setSkillName(dto.getSkillName());
        if (dto.getCoverImage() != null) skill.setCoverImage(dto.getCoverImage());
        if (dto.getDescription() != null) skill.setDescription(dto.getDescription());
        if (dto.getPrice() != null) skill.setPrice(dto.getPrice());

        skillMapper.updateById(skill);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> deleteSkill(Long userId, Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            return R.fail("Skill not found");
        }

        if (!skill.getUserId().equals(userId)) {
            return R.fail("Not authorized");
        }

        skillMapper.deleteById(skillId);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> toggleSkillOnline(Long userId, Long skillId, Boolean isOnline) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            return R.fail("Skill not found");
        }

        if (!skill.getUserId().equals(userId)) {
            return R.fail("Not authorized");
        }

        skill.setIsOnline(isOnline);
        skillMapper.updateById(skill);

        return R.ok();
    }

    @Override
    public R<SkillDetailVo> getSkillDetail(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            return R.fail("Skill not found");
        }

        // Get images
        List<SkillImage> images = skillImageMapper.selectBySkillId(skillId);
        List<String> imageUrls = images.stream()
            .map(SkillImage::getImageUrl)
            .collect(Collectors.toList());

        // Get promises
        List<SkillPromise> promises = skillPromiseMapper.selectBySkillId(skillId);
        List<String> promiseTexts = promises.stream()
            .map(SkillPromise::getPromiseText)
            .collect(Collectors.toList());

        // Build VO
        SkillDetailVo vo = SkillDetailVo.builder()
            .skillId(skill.getSkillId())
            .userId(skill.getUserId())
            .skillName(skill.getSkillName())
            .skillType(skill.getSkillType())
            .coverImage(skill.getCoverImage())
            .description(skill.getDescription())
            .price(skill.getPrice())
            .priceUnit(skill.getPriceUnit())
            .isOnline(skill.getIsOnline())
            .rating(skill.getRating())
            .reviewCount(skill.getReviewCount())
            .orderCount(skill.getOrderCount())
            .gameName(skill.getGameName())
            .gameRank(skill.getGameRank())
            .serviceHours(skill.getServiceHours())
            .serviceType(skill.getServiceType())
            .serviceLocation(skill.getServiceLocation())
            .latitude(skill.getLatitude())
            .longitude(skill.getLongitude())
            .images(imageUrls)
            .promises(promiseTexts)
            .build();

        return R.ok(vo);
    }

    @Override
    public TableDataInfo<SkillVo> getMySkills(Long userId, PageQuery pageQuery) {
        Page<Skill> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Skill::getUserId, userId)
            .orderByDesc(Skill::getCreatedAt);

        Page<Skill> skillPage = skillMapper.selectPage(page, wrapper);

        List<SkillVo> voList = skillPage.getRecords().stream()
            .map(this::buildSkillVo)
            .collect(Collectors.toList());

        return new TableDataInfo<>(voList, skillPage.getTotal());
    }

    @Override
    public TableDataInfo<SkillVo> getUserSkills(Long userId, PageQuery pageQuery) {
        List<Skill> skills = skillMapper.selectOnlineSkillsByUserId(userId);

        List<SkillVo> voList = skills.stream()
            .map(this::buildSkillVo)
            .collect(Collectors.toList());

        return TableDataInfo.build(voList);
    }

    @Override
    public TableDataInfo<SkillVo> searchNearbySkills(BigDecimal latitude, BigDecimal longitude, Integer radiusMeters, PageQuery pageQuery) {
        List<Skill> skills = skillMapper.findNearbySkills(latitude, longitude, radiusMeters, 100);

        List<SkillVo> voList = skills.stream()
            .map(this::buildSkillVo)
            .collect(Collectors.toList());

        return TableDataInfo.build(voList);
    }

    private SkillVo buildSkillVo(Skill skill) {
        return SkillVo.builder()
            .skillId(skill.getSkillId())
            .skillName(skill.getSkillName())
            .skillType(skill.getSkillType())
            .coverImage(skill.getCoverImage())
            .price(skill.getPrice())
            .priceUnit(skill.getPriceUnit())
            .isOnline(skill.getIsOnline())
            .rating(skill.getRating())
            .reviewCount(skill.getReviewCount())
            .orderCount(skill.getOrderCount())
            .gameName(skill.getGameName())
            .gameRank(skill.getGameRank())
            .serviceType(skill.getServiceType())
            .serviceLocation(skill.getServiceLocation())
            .build();
    }

    @Override
    public SkilledUsersResultVo getSkilledUsers(SkilledUsersQueryDto queryDto) {
        // 1. 从数据库查询有技能的用户列表
        List<LimitedTimeUserVo> users = userMapper.queryLimitedTimeUsers(
            queryDto.getGender(),
            queryDto.getCityCode(),
            queryDto.getDistrictCode(),
            queryDto.getLatitude(),
            queryDto.getLongitude(),
            (queryDto.getPageNum() - 1) * queryDto.getPageSize(),
            queryDto.getPageSize()
        );

        // 2. 获取总数
        Integer total = userMapper.countLimitedTimeUsers(
            queryDto.getGender(),
            queryDto.getCityCode(),
            queryDto.getDistrictCode()
        );

        // 3. 转换为 SkilledUserVo
        List<SkilledUserVo> skilledUsers = users.stream()
            .map(this::convertToSkilledUser)
            .collect(Collectors.toList());

        // 4. 应用排序
        skilledUsers = applySorting(skilledUsers, queryDto.getSortBy());

        // 5. 计算是否有更多
        boolean hasMore = (queryDto.getPageNum() * queryDto.getPageSize()) < total;

        // 6. 构建响应
        return SkilledUsersResultVo.builder()
            .total(total)
            .hasMore(hasMore)
            .filters(buildFilterOptions())
            .list(skilledUsers)
            .build();
    }

    /**
     * 转换为 SkilledUserVo
     */
    private SkilledUserVo convertToSkilledUser(LimitedTimeUserVo rpcUser) {
        // 计算促销价格 (临时Mock 7折优惠)
        int originalPrice = rpcUser.getPrice() != null ? rpcUser.getPrice().intValue() : 15;
        int promotionPrice = (int) (originalPrice * 0.7);

        // 选择促销标签
        String promotionTag = PROMOTION_TAGS[(rpcUser.getUserId().intValue()) % PROMOTION_TAGS.length];

        // 构建标签列表
        List<SkilledUserVo.UserTag> tags = new ArrayList<>();

        // 在线标签
        if (rpcUser.getIsOnline() != null && rpcUser.getIsOnline()) {
            tags.add(SkilledUserVo.UserTag.builder()
                .text("可线上")
                .type("feature")
                .color("#4CAF50")
                .build());
        }

        // 促销标签
        tags.add(SkilledUserVo.UserTag.builder()
            .text("限时7折")
            .type("price")
            .color("#FF5722")
            .build());

        // 技能标签
        if (rpcUser.getSkillName() != null) {
            tags.add(SkilledUserVo.UserTag.builder()
                .text(rpcUser.getSkillName())
                .type("skill")
                .color("#2196F3")
                .build());
        }

        return SkilledUserVo.builder()
            .userId(rpcUser.getUserId())
            .avatar(rpcUser.getAvatar())
            .nickname(rpcUser.getNickname())
            .gender(rpcUser.getGender())
            .age(rpcUser.getAge() != null ? rpcUser.getAge() : 0)
            .distance(rpcUser.getDistance() != null ? rpcUser.getDistance() : 0)
            .distanceText(formatDistance(rpcUser.getDistance() != null ? rpcUser.getDistance() : 0))
            .tags(tags)
            .description(rpcUser.getBio() != null && !rpcUser.getBio().isEmpty() ?
                rpcUser.getBio() : "资深陪玩师，服务态度好，技术过硬！")
            .price(SkilledUserVo.PriceInfo.builder()
                .amount(promotionPrice)
                .unit(rpcUser.getPriceUnit() != null ? rpcUser.getPriceUnit() : "per_order")
                .displayText(promotionPrice + " 金币/" +
                    (rpcUser.getPriceUnit() != null && rpcUser.getPriceUnit().equals("小时") ? "小时" : "单"))
                .originalPrice(originalPrice)
                .build())
            .promotionTag(promotionTag)
            .isOnline(rpcUser.getIsOnline() != null ? rpcUser.getIsOnline() : false)
            .skillLevel(rpcUser.getSkillLevel() != null ? rpcUser.getSkillLevel() : "熟练")
            .skillId(rpcUser.getSkillId())
            .skillName(rpcUser.getSkillName())
            .build();
    }

    /**
     * 应用排序
     */
    private List<SkilledUserVo> applySorting(List<SkilledUserVo> users, String sortBy) {
        if (sortBy == null || "smart_recommend".equals(sortBy)) {
            // 智能推荐排序
            return users.stream()
                .sorted(Comparator.comparingInt(u ->
                    -(u.getIsOnline() ? 1000 : 0)
                        - (1000 - u.getDistance() / 100)
                        - (100 - u.getPrice().getAmount())))
                .collect(Collectors.toList());
        } else if ("price_asc".equals(sortBy)) {
            return users.stream()
                .sorted(Comparator.comparingInt(u -> u.getPrice().getAmount()))
                .collect(Collectors.toList());
        } else if ("price_desc".equals(sortBy)) {
            return users.stream()
                .sorted(Comparator.comparingInt(u -> -u.getPrice().getAmount()))
                .collect(Collectors.toList());
        } else if ("distance_asc".equals(sortBy)) {
            return users.stream()
                .sorted(Comparator.comparingInt(SkilledUserVo::getDistance))
                .collect(Collectors.toList());
        }
        return users;
    }

    /**
     * 构建筛选选项
     */
    private SkilledUsersResultVo.FilterOptions buildFilterOptions() {
        return SkilledUsersResultVo.FilterOptions.builder()
            .sortOptions(Arrays.asList(
                SkilledUsersResultVo.Option.builder().value("smart_recommend").label("智能推荐").build(),
                SkilledUsersResultVo.Option.builder().value("price_asc").label("价格从低到高").build(),
                SkilledUsersResultVo.Option.builder().value("price_desc").label("价格从高到低").build(),
                SkilledUsersResultVo.Option.builder().value("distance_asc").label("距离最近").build()
            ))
            .genderOptions(Arrays.asList(
                SkilledUsersResultVo.Option.builder().value("all").label("不限性别").build(),
                SkilledUsersResultVo.Option.builder().value("male").label("男").build(),
                SkilledUsersResultVo.Option.builder().value("female").label("女").build()
            ))
            .languageOptions(Arrays.asList(
                SkilledUsersResultVo.Option.builder().value("all").label("语言(不限)").build(),
                SkilledUsersResultVo.Option.builder().value("mandarin").label("普通话").build(),
                SkilledUsersResultVo.Option.builder().value("cantonese").label("粤语").build(),
                SkilledUsersResultVo.Option.builder().value("english").label("英语").build()
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
}
