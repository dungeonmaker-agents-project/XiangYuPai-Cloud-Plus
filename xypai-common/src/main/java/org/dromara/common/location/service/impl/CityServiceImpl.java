package org.dromara.common.location.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.location.domain.entity.City;
import org.dromara.common.location.domain.vo.CityGroupVo;
import org.dromara.common.location.domain.vo.CityInfoVo;
import org.dromara.common.location.domain.vo.CityListResultVo;
import org.dromara.common.location.mapper.CityMapper;
import org.dromara.common.location.service.ICityService;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 城市服务实现
 * City Service Implementation
 *
 * @author XiangYuPai Team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CityServiceImpl implements ICityService {

    private final CityMapper cityMapper;

    /**
     * 城市列表缓存Key
     */
    private static final String CITY_LIST_CACHE_KEY = "location:cities:all";

    /**
     * 缓存过期时间: 24小时
     */
    private static final Duration CACHE_DURATION = Duration.ofHours(24);

    @Override
    public R<CityListResultVo> getCityList(Long userId) {
        log.info("获取城市列表 - 用户ID: {}", userId);

        // 1. 尝试从缓存获取
        CityListResultVo cached = RedisUtils.getCacheObject(CITY_LIST_CACHE_KEY);
        if (cached != null) {
            log.debug("从缓存获取城市列表成功");
            return R.ok(cached);
        }

        // 2. 查询所有正常状态的城市
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(City::getStatus, 1)      // 1=正常
               .eq(City::getDeleted, 0)     // 未删除
               .orderByAsc(City::getPinyin); // 按拼音排序
        List<City> allCities = cityMapper.selectList(wrapper);

        // 3. 查询热门城市
        LambdaQueryWrapper<City> hotWrapper = new LambdaQueryWrapper<>();
        hotWrapper.eq(City::getIsHot, 1)    // 热门城市
                  .eq(City::getStatus, 1)
                  .eq(City::getDeleted, 0)
                  .orderByAsc(City::getSortOrder); // 按排序权重排序
        List<City> hotCities = cityMapper.selectList(hotWrapper);

        // 4. 按首字母分组
        Map<String, List<CityInfoVo>> groupedCities = allCities.stream()
            .map(city -> BeanUtil.toBean(city, CityInfoVo.class))
            .collect(Collectors.groupingBy(city ->
                String.valueOf(city.getFirstLetter()).toUpperCase()));

        List<CityGroupVo> cityGroups = groupedCities.entrySet().stream()
            .map(entry -> {
                CityGroupVo group = new CityGroupVo();
                group.setLetter(entry.getKey());
                group.setCities(entry.getValue());
                return group;
            })
            .sorted(Comparator.comparing(CityGroupVo::getLetter))
            .toList();

        // 5. 构建结果
        CityListResultVo result = new CityListResultVo();
        result.setHotCities(BeanUtil.copyToList(hotCities, CityInfoVo.class));
        result.setAllCities(cityGroups);

        // 6. 缓存结果
        RedisUtils.setCacheObject(CITY_LIST_CACHE_KEY, result, CACHE_DURATION);
        log.info("城市列表已缓存 - 热门城市: {}, 全部城市: {}",
                 result.getHotCities().size(), allCities.size());

        return R.ok(result);
    }

    @Override
    public CityInfoVo getByCityCode(String cityCode) {
        log.debug("根据城市代码查询城市信息 - cityCode: {}", cityCode);

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(City::getCityCode, cityCode)
               .eq(City::getDeleted, 0);

        return cityMapper.selectVoOne(wrapper);
    }
}
