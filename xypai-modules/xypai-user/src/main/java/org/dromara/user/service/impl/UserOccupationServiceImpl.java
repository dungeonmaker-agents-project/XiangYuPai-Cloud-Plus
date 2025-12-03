package org.dromara.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.dto.UpdateOccupationsDto;
import org.dromara.user.domain.entity.UserOccupation;
import org.dromara.user.mapper.UserOccupationMapper;
import org.dromara.user.service.IUserOccupationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户职业服务实现（支持多职业）
 * User Occupation Service Implementation
 *
 * 对应UI文档: 个人主页-编辑_结构文档.md
 * - 支持多职业标签选择
 * - 最多5个职业
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOccupationServiceImpl extends ServiceImpl<UserOccupationMapper, UserOccupation> implements IUserOccupationService {

    private static final int MAX_OCCUPATION_COUNT = 5;

    private final UserOccupationMapper userOccupationMapper;

    @Override
    public R<List<String>> getUserOccupations(Long userId) {
        if (userId == null) {
            return R.fail("用户ID不能为空");
        }

        List<String> occupations = userOccupationMapper.selectOccupationNamesByUserId(userId);
        return R.ok(occupations != null ? occupations : new ArrayList<>());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateOccupations(Long userId, UpdateOccupationsDto dto) {
        if (userId == null) {
            return R.fail("用户ID不能为空");
        }

        if (dto == null || dto.getOccupations() == null) {
            return R.fail("职业列表不能为空");
        }

        List<String> occupations = dto.getOccupations();

        // 验证职业数量
        if (occupations.size() > MAX_OCCUPATION_COUNT) {
            return R.fail("最多选择" + MAX_OCCUPATION_COUNT + "个职业");
        }

        // 验证每个职业名称长度
        for (String occupation : occupations) {
            if (occupation == null || occupation.trim().isEmpty()) {
                return R.fail("职业名称不能为空");
            }
            if (occupation.length() > 30) {
                return R.fail("职业名称不能超过30字符");
            }
        }

        // 删除现有职业
        userOccupationMapper.deleteAllByUserId(userId);

        // 插入新职业
        for (int i = 0; i < occupations.size(); i++) {
            String occupationName = occupations.get(i).trim();
            UserOccupation occupation = UserOccupation.builder()
                .userId(userId)
                .occupationName(occupationName)
                .sortOrder(i)
                .build();
            userOccupationMapper.insert(occupation);
        }

        log.info("用户 {} 更新职业列表: {}", userId, occupations);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> addOccupation(Long userId, String occupationName) {
        if (userId == null) {
            return R.fail("用户ID不能为空");
        }

        if (occupationName == null || occupationName.trim().isEmpty()) {
            return R.fail("职业名称不能为空");
        }

        occupationName = occupationName.trim();
        if (occupationName.length() > 30) {
            return R.fail("职业名称不能超过30字符");
        }

        // 检查数量限制
        Integer currentCount = userOccupationMapper.countByUserId(userId);
        if (currentCount >= MAX_OCCUPATION_COUNT) {
            return R.fail("最多选择" + MAX_OCCUPATION_COUNT + "个职业");
        }

        // 插入新职业
        UserOccupation occupation = UserOccupation.builder()
            .userId(userId)
            .occupationName(occupationName)
            .sortOrder(currentCount)
            .build();
        userOccupationMapper.insert(occupation);

        log.info("用户 {} 添加职业: {}", userId, occupationName);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> removeOccupation(Long userId, String occupationName) {
        if (userId == null) {
            return R.fail("用户ID不能为空");
        }

        if (occupationName == null || occupationName.trim().isEmpty()) {
            return R.fail("职业名称不能为空");
        }

        // 查找并删除
        List<UserOccupation> occupations = userOccupationMapper.selectByUserId(userId);
        for (UserOccupation occupation : occupations) {
            if (occupation.getOccupationName().equals(occupationName.trim())) {
                userOccupationMapper.deleteById(occupation.getOccupationId());
                log.info("用户 {} 删除职业: {}", userId, occupationName);
                return R.ok();
            }
        }

        return R.fail("未找到该职业");
    }

    @Override
    public Integer countOccupations(Long userId) {
        if (userId == null) {
            return 0;
        }
        Integer count = userOccupationMapper.countByUserId(userId);
        return count != null ? count : 0;
    }
}
