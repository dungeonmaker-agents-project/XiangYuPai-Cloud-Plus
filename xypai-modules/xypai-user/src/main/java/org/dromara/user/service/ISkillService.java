package org.dromara.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.domain.entity.Skill;
import org.dromara.user.domain.dto.SkillCreateDto;
import org.dromara.user.domain.dto.SkillUpdateDto;
import org.dromara.user.domain.dto.OnlineSkillCreateDto;
import org.dromara.user.domain.dto.OfflineSkillCreateDto;
import org.dromara.user.domain.vo.SkillDetailVo;
import org.dromara.user.domain.vo.SkillVo;

import java.math.BigDecimal;

/**
 * 技能服务接口
 * Skill Service Interface
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
public interface ISkillService extends IService<Skill> {

    /**
     * 创建技能（通用）
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 技能ID
     */
    R<Long> createSkill(Long userId, SkillCreateDto dto);

    /**
     * 创建线上技能
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 技能ID
     */
    R<Long> createOnlineSkill(Long userId, OnlineSkillCreateDto dto);

    /**
     * 创建线下技能
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 技能ID
     */
    R<Long> createOfflineSkill(Long userId, OfflineSkillCreateDto dto);

    /**
     * 更新技能
     *
     * @param userId  用户ID
     * @param skillId 技能ID
     * @param dto     DTO
     * @return 结果
     */
    R<Void> updateSkill(Long userId, Long skillId, SkillUpdateDto dto);

    /**
     * 删除技能
     *
     * @param userId  用户ID
     * @param skillId 技能ID
     * @return 结果
     */
    R<Void> deleteSkill(Long userId, Long skillId);

    /**
     * 上架/下架技能
     *
     * @param userId   用户ID
     * @param skillId  技能ID
     * @param isOnline 是否上架
     * @return 结果
     */
    R<Void> toggleSkillOnline(Long userId, Long skillId, Boolean isOnline);

    /**
     * 获取技能详情
     *
     * @param skillId 技能ID
     * @return 技能详情
     */
    R<SkillDetailVo> getSkillDetail(Long skillId);

    /**
     * 获取我的技能列表
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 技能列表
     */
    TableDataInfo<SkillVo> getMySkills(Long userId, PageQuery pageQuery);

    /**
     * 获取用户的技能列表
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 技能列表
     */
    TableDataInfo<SkillVo> getUserSkills(Long userId, PageQuery pageQuery);

    /**
     * 搜索附近的技能
     *
     * @param latitude     纬度
     * @param longitude    经度
     * @param radiusMeters 半径（米）
     * @param pageQuery    分页参数
     * @return 技能列表
     */
    TableDataInfo<SkillVo> searchNearbySkills(BigDecimal latitude, BigDecimal longitude, Integer radiusMeters, PageQuery pageQuery);
}
