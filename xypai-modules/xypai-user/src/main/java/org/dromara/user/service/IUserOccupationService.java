package org.dromara.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.dto.UpdateOccupationsDto;
import org.dromara.user.domain.entity.UserOccupation;

import java.util.List;

/**
 * 用户职业服务接口（支持多职业）
 * User Occupation Service Interface
 *
 * 对应UI文档: 个人主页-编辑_结构文档.md
 * - 支持多职业标签选择
 * - 最多5个职业
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
public interface IUserOccupationService extends IService<UserOccupation> {

    /**
     * 获取用户职业列表
     *
     * @param userId 用户ID
     * @return 职业名称列表
     */
    R<List<String>> getUserOccupations(Long userId);

    /**
     * 更新用户职业列表（多选）
     * 会先删除所有现有职业，再插入新职业
     *
     * @param userId 用户ID
     * @param dto    职业更新DTO
     * @return 结果
     */
    R<Void> updateOccupations(Long userId, UpdateOccupationsDto dto);

    /**
     * 添加单个职业
     *
     * @param userId         用户ID
     * @param occupationName 职业名称
     * @return 结果
     */
    R<Void> addOccupation(Long userId, String occupationName);

    /**
     * 删除单个职业
     *
     * @param userId         用户ID
     * @param occupationName 职业名称
     * @return 结果
     */
    R<Void> removeOccupation(Long userId, String occupationName);

    /**
     * 获取用户职业数量
     *
     * @param userId 用户ID
     * @return 职业数量
     */
    Integer countOccupations(Long userId);
}
