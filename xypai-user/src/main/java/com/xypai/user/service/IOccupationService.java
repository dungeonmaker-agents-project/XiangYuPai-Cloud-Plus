package com.xypai.user.service;

import com.xypai.user.domain.dto.UserOccupationUpdateDTO;
import com.xypai.user.domain.vo.OccupationDictVO;
import com.xypai.user.domain.vo.UserOccupationVO;

import java.util.List;

/**
 * 职业标签服务接口
 *
 * @author Bob
 * @date 2025-01-14
 */
public interface IOccupationService {

    /**
     * 查询所有启用的职业（按排序）
     */
    List<OccupationDictVO> listAllOccupations();

    /**
     * 根据分类查询职业
     */
    List<OccupationDictVO> listOccupationsByCategory(String category);

    /**
     * 查询所有职业分类
     */
    List<String> listAllCategories();

    /**
     * 查询用户的职业标签
     */
    List<UserOccupationVO> getUserOccupations(Long userId);

    /**
     * 更新用户职业标签（批量）
     * 最多5个职业
     */
    boolean updateUserOccupations(Long userId, UserOccupationUpdateDTO updateDTO);

    /**
     * 添加单个职业标签
     */
    boolean addUserOccupation(Long userId, String occupationCode);

    /**
     * 删除单个职业标签
     */
    boolean removeUserOccupation(Long userId, String occupationCode);

    /**
     * 清空用户所有职业标签
     */
    boolean clearUserOccupations(Long userId);

    /**
     * 检查用户是否有某个职业
     */
    boolean hasOccupation(Long userId, String occupationCode);

    /**
     * 统计拥有某个职业的用户数量
     */
    int countUsersByOccupation(String occupationCode);

    /**
     * 查询拥有某个职业的用户ID列表
     */
    List<Long> getUserIdsByOccupation(String occupationCode);
}

