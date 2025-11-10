package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.core.exception.ServiceException;
import com.xypai.user.domain.dto.UserOccupationUpdateDTO;
import com.xypai.user.domain.entity.OccupationDict;
import com.xypai.user.domain.entity.UserOccupation;
import com.xypai.user.domain.vo.OccupationDictVO;
import com.xypai.user.domain.vo.UserOccupationVO;
import com.xypai.user.mapper.OccupationDictMapper;
import com.xypai.user.mapper.UserOccupationMapper;
import com.xypai.user.service.IOccupationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 职业标签服务实现
 *
 * @author Bob
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OccupationServiceImpl implements IOccupationService {

    private final OccupationDictMapper occupationDictMapper;
    private final UserOccupationMapper userOccupationMapper;

    private static final int MAX_OCCUPATION_COUNT = 5;

    /**
     * 查询所有启用的职业（按排序�?
     */
    @Override
    public List<OccupationDictVO> listAllOccupations() {
        List<OccupationDict> occupations = occupationDictMapper.selectEnabledOccupations();
        return occupations.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    /**
     * 根据分类查询职业
     */
    @Override
    public List<OccupationDictVO> listOccupationsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return new ArrayList<>();
        }

        List<OccupationDict> occupations = occupationDictMapper.selectByCategory(category);
        return occupations.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    /**
     * 查询所有职业分�?
     */
    @Override
    public List<String> listAllCategories() {
        return occupationDictMapper.selectAllCategories();
    }

    /**
     * 查询用户的职业标�?
     */
    @Override
    public List<UserOccupationVO> getUserOccupations(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        // 查询用户职业
        List<UserOccupation> userOccupations = userOccupationMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(userOccupations)) {
            return new ArrayList<>();
        }

        // 查询职业详情
        List<String> codes = userOccupations.stream()
            .map(UserOccupation::getOccupationCode)
            .collect(Collectors.toList());
        
        List<OccupationDict> occupationDicts = occupationDictMapper.selectBatchByCodes(codes);
        Map<String, OccupationDict> dictMap = occupationDicts.stream()
            .collect(Collectors.toMap(OccupationDict::getCode, dict -> dict));

        // 组装VO
        return userOccupations.stream()
            .map(uo -> {
                UserOccupationVO vo = new UserOccupationVO();
                vo.setId(uo.getId());
                vo.setUserId(uo.getUserId());
                vo.setOccupationCode(uo.getOccupationCode());
                vo.setSortOrder(uo.getSortOrder());
                vo.setCreatedAt(uo.getCreatedAt());
                vo.setIsPrimary(uo.isPrimary());

                // 填充职业详情
                OccupationDict dict = dictMap.get(uo.getOccupationCode());
                if (dict != null) {
                    vo.setOccupationName(dict.getName());
                    vo.setCategory(dict.getCategory());
                    vo.setIconUrl(dict.getIconUrl());
                }

                return vo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 更新用户职业标签（批量）
     * 最�?个职�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserOccupations(Long userId, UserOccupationUpdateDTO updateDTO) {
        if (userId == null || updateDTO == null) {
            throw new ServiceException("参数不能为空");
        }

        List<String> occupationCodes = updateDTO.getOccupationCodes();
        if (CollectionUtils.isEmpty(occupationCodes)) {
            // 清空所有职�?
            return clearUserOccupations(userId);
        }

        // 验证数量
        if (occupationCodes.size() > MAX_OCCUPATION_COUNT) {
            throw new ServiceException("最多只能选择" + MAX_OCCUPATION_COUNT + "个职业");
        }

        // 去重
        occupationCodes = occupationCodes.stream()
            .distinct()
            .collect(Collectors.toList());

        // 验证职业编码是否存在
        List<OccupationDict> validOccupations = occupationDictMapper.selectBatchByCodes(occupationCodes);
        if (validOccupations.size() != occupationCodes.size()) {
            throw new ServiceException("部分职业编码不存在或已禁用");
        }

        // 删除原有职业
        userOccupationMapper.deleteByUserId(userId);

        // 插入新职�?
        List<UserOccupation> newOccupations = new ArrayList<>();
        for (int i = 0; i < occupationCodes.size(); i++) {
            UserOccupation occupation = UserOccupation.builder()
                .userId(userId)
                .occupationCode(occupationCodes.get(i))
                .sortOrder(i)
                .build();
            newOccupations.add(occupation);
        }

        // 批量插入
        int count = 0;
        for (UserOccupation occupation : newOccupations) {
            count += userOccupationMapper.insert(occupation);
        }

        log.info("更新用户职业成功，userId: {}, count: {}", userId, count);
        return count > 0;
    }

    /**
     * 添加单个职业标签
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserOccupation(Long userId, String occupationCode) {
        if (userId == null || occupationCode == null || occupationCode.isEmpty()) {
            throw new ServiceException("参数不能为空");
        }

        // 检查是否已存在
        if (hasOccupation(userId, occupationCode)) {
            throw new ServiceException("该职业标签已存在");
        }

        // 检查数量限制
        int currentCount = userOccupationMapper.countByUserId(userId);
        if (currentCount >= MAX_OCCUPATION_COUNT) {
            throw new ServiceException("最多只能选择" + MAX_OCCUPATION_COUNT + "个职业");
        }

        // 验证职业编码
        OccupationDict occupation = occupationDictMapper.selectById(occupationCode);
        if (occupation == null || !occupation.isEnabled()) {
            throw new ServiceException("职业编码不存在或已禁用");
        }

        // 插入
        UserOccupation userOccupation = UserOccupation.builder()
            .userId(userId)
            .occupationCode(occupationCode)
            .sortOrder(currentCount)
            .build();

        int result = userOccupationMapper.insert(userOccupation);
        log.info("添加用户职业成功，userId: {}, code: {}", userId, occupationCode);
        return result > 0;
    }

    /**
     * 删除单个职业标签
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserOccupation(Long userId, String occupationCode) {
        if (userId == null || occupationCode == null || occupationCode.isEmpty()) {
            throw new ServiceException("参数不能为空");
        }

        LambdaQueryWrapper<UserOccupation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOccupation::getUserId, userId)
               .eq(UserOccupation::getOccupationCode, occupationCode);

        int result = userOccupationMapper.delete(wrapper);
        log.info("删除用户职业成功，userId: {}, code: {}", userId, occupationCode);
        return result > 0;
    }

    /**
     * 清空用户所有职业标�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearUserOccupations(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        int result = userOccupationMapper.deleteByUserId(userId);
        log.info("清空用户职业成功，userId: {}, count: {}", userId, result);
        return result >= 0;
    }

    /**
     * 检查用户是否有某个职业
     */
    @Override
    public boolean hasOccupation(Long userId, String occupationCode) {
        if (userId == null || occupationCode == null || occupationCode.isEmpty()) {
            return false;
        }
        return userOccupationMapper.existsByUserIdAndCode(userId, occupationCode);
    }

    /**
     * 统计拥有某个职业的用户数�?
     */
    @Override
    public int countUsersByOccupation(String occupationCode) {
        if (occupationCode == null || occupationCode.isEmpty()) {
            return 0;
        }

        List<Long> userIds = userOccupationMapper.selectUserIdsByOccupationCode(occupationCode);
        return userIds.size();
    }

    /**
     * 查询拥有某个职业的用户ID列表
     */
    @Override
    public List<Long> getUserIdsByOccupation(String occupationCode) {
        if (occupationCode == null || occupationCode.isEmpty()) {
            return new ArrayList<>();
        }
        return userOccupationMapper.selectUserIdsByOccupationCode(occupationCode);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换Entity为VO
     */
    private OccupationDictVO convertToVO(OccupationDict entity) {
        OccupationDictVO vo = new OccupationDictVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusDesc(entity.getStatusDesc());
        vo.setHasIcon(entity.hasIcon());
        return vo;
    }
}

