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
import org.dromara.user.domain.vo.*;
import org.dromara.user.mapper.*;
import org.dromara.user.service.ISkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
}
