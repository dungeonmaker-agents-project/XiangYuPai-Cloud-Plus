package org.dromara.common.location.service;

import org.dromara.common.core.domain.R;
import org.dromara.common.location.domain.vo.CityInfoVo;
import org.dromara.common.location.domain.vo.CityListResultVo;

/**
 * 城市服务接口
 * City Service Interface
 *
 * @author XiangYuPai Team
 */
public interface ICityService {

    /**
     * 获取城市列表 (含热门城市和全部城市分组)
     *
     * @param userId 用户ID (可选，用于个性化排序)
     * @return 城市列表结果
     */
    R<CityListResultVo> getCityList(Long userId);

    /**
     * 根据城市代码获取城市信息
     *
     * @param cityCode 城市代码
     * @return 城市信息
     */
    CityInfoVo getByCityCode(String cityCode);

    /**
     * 根据城市名称查询城市代码
     *
     * @param cityName 城市名称
     * @return 城市代码 (如果找不到返回null)
     */
    String getCityCodeByName(String cityName);
}
