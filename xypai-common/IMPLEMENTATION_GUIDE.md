# XiangYuPai Common Module Implementation Guide

## 项目概述

本文档提供了将 `ruoyi-example-common` 转换为 `xypai-common` 业务通用服务模块的完整实现指南。

**目标**: 创建4个核心业务服务:
1. **xypai-location** - 位置服务
2. **xypai-media** - 媒体上传服务
3. **xypai-notification** - 通知服务
4. **xypai-report** - 举报审核服务

---

## 项目结构

```
xypai-common/
├── pom.xml                              # 父POM
├── xypai-location/                      # 位置服务
│   ├── pom.xml
│   ├── src/main/java/org/dromara/location/
│   │   ├── LocationApplication.java     # 启动类
│   │   ├── controller/
│   │   │   ├── app/                     # C端API
│   │   │   │   ├── LocationController.java
│   │   │   │   └── CityController.java
│   │   │   ├── admin/                   # B端管理API
│   │   │   │   └── CityManageController.java
│   │   │   └── feign/                   # RPC接口
│   │   │       └── RemoteLocationServiceImpl.java
│   │   ├── service/
│   │   │   ├── ILocationService.java
│   │   │   ├── ICityService.java
│   │   │   └── impl/
│   │   │       ├── LocationServiceImpl.java    # ✅ 已创建
│   │   │       └── CityServiceImpl.java
│   │   ├── mapper/
│   │   │   ├── LocationMapper.java             # ✅ 已创建
│   │   │   └── CityMapper.java                 # ✅ 已创建
│   │   └── domain/
│   │       ├── entity/
│   │       │   ├── Location.java               # ✅ 已创建
│   │       │   └── City.java                   # ✅ 已创建
│   │       ├── bo/
│   │       │   ├── NearbyLocationQueryBo.java  # ✅ 已创建
│   │       │   └── CitySelectBo.java
│   │       └── vo/
│   │           ├── LocationListVo.java         # ✅ 已创建
│   │           ├── CityInfoVo.java             # ✅ 已创建
│   │           ├── CityListResultVo.java       # ✅ 已创建
│   │           └── CityGroupVo.java            # ✅ 已创建
│   └── src/main/resources/
│       ├── application.yml
│       ├── bootstrap.yml
│       ├── logback-plus.xml
│       └── mapper/location/
│           ├── LocationMapper.xml
│           └── CityMapper.xml
├── xypai-media/                         # 媒体上传服务
├── xypai-notification/                  # 通知服务
└── xypai-report/                        # 举报审核服务
```

---

## 已完成文件清单

### xypai-location (位置服务)

#### ✅ 核心文件已创建:
1. `pom.xml` - Maven配置
2. `LocationApplication.java` - Spring Boot启动类
3. **Entity层**:
   - `Location.java` - 地点实体
   - `City.java` - 城市实体
4. **VO层**:
   - `LocationListVo.java` - 地点列表视图
   - `CityInfoVo.java` - 城市信息视图
   - `CityListResultVo.java` - 城市列表结果
   - `CityGroupVo.java` - 城市分组视图
5. **BO层**:
   - `NearbyLocationQueryBo.java` - 附近地点查询参数
6. **Mapper层**:
   - `LocationMapper.java` - 地点数据访问
   - `CityMapper.java` - 城市数据访问
7. **Service层**:
   - `ILocationService.java` - 位置服务接口
   - `LocationServiceImpl.java` - 位置服务实现(含距离计算)
   - `ICityService.java` - 城市服务接口

---

## 待完成任务

### 1. xypai-location 待完成文件

#### Controller层
```java
// src/main/java/org/dromara/location/controller/app/LocationController.java
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/location")
public class LocationController extends BaseController {

    private final ILocationService locationService;

    /**
     * 获取附近地点
     */
    @SaCheckRole("user")
    @GetMapping("/nearby")
    public TableDataInfo<LocationListVo> getNearbyLocations(@Validated NearbyLocationQueryBo query) {
        return locationService.queryNearbyLocations(query);
    }

    /**
     * 搜索地点
     */
    @GetMapping("/search")
    public TableDataInfo<LocationListVo> searchLocations(@Validated LocationSearchBo query) {
        return locationService.searchLocations(query);
    }
}
```

#### CityServiceImpl
```java
// src/main/java/org/dromara/location/service/impl/CityServiceImpl.java
@Slf4j
@RequiredArgsConstructor
@Service
public class CityServiceImpl implements ICityService {

    private final CityMapper cityMapper;
    private final RedisService redisService;

    @Override
    public R<CityListResultVo> getCityList(Long userId) {
        String cacheKey = "location:cities:all";

        // 1. 尝试缓存
        CityListResultVo cached = redisService.getCacheObject(cacheKey);
        if (cached != null) {
            return R.ok(cached);
        }

        // 2. 查询所有城市
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(City::getStatus, 1)
               .eq(City::getDeleted, 0)
               .orderByAsc(City::getPinyin);
        List<City> allCities = cityMapper.selectList(wrapper);

        // 3. 查询热门城市
        LambdaQueryWrapper<City> hotWrapper = new LambdaQueryWrapper<>();
        hotWrapper.eq(City::getIsHot, 1)
                  .eq(City::getStatus, 1)
                  .orderByAsc(City::getSortOrder);
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

        // 6. 缓存24小时
        redisService.setCacheObject(cacheKey, result, Duration.ofHours(24));

        return R.ok(result);
    }

    @Override
    public CityInfoVo getByCityCode(String cityCode) {
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(City::getCityCode, cityCode)
               .eq(City::getDeleted, 0);
        return cityMapper.selectVoOne(wrapper);
    }
}
```

#### Dubbo RPC实现
```java
// src/main/java/org/dromara/location/controller/feign/RemoteLocationServiceImpl.java
@DubboService
@Service
@RequiredArgsConstructor
public class RemoteLocationServiceImpl implements RemoteLocationService {

    private final ILocationService locationService;
    private final ICityService cityService;

    @Override
    public R<DistanceVo> calculateDistance(BigDecimal fromLat, BigDecimal fromLng,
                                          BigDecimal toLat, BigDecimal toLng) {
        // 计算距离
        BigDecimal distance = locationService.calculateDistance(fromLat, fromLng, toLat, toLng);

        DistanceVo vo = new DistanceVo();
        vo.setDistance(distance);
        vo.setUnit("km");
        vo.setDisplayText(locationService.formatDistance(distance));

        return R.ok(vo);
    }

    @Override
    public R<List<DistanceVo>> calculateBatchDistance(BigDecimal fromLat, BigDecimal fromLng,
                                                      List<LocationPointDto> targets) {
        List<DistanceVo> distances = targets.stream()
            .map(target -> {
                BigDecimal distance = locationService.calculateDistance(
                    fromLat, fromLng, target.getLatitude(), target.getLongitude()
                );

                DistanceVo vo = new DistanceVo();
                vo.setId(target.getId());
                vo.setDistance(distance);
                vo.setDisplayText(locationService.formatDistance(distance));
                return vo;
            })
            .toList();

        return R.ok(distances);
    }
}
```

#### 配置文件
```yaml
# src/main/resources/application.yml
server:
  port: 9407

spring:
  application:
    name: xypai-location
  profiles:
    active: @profiles.active@
```

```yaml
# src/main/resources/bootstrap.yml
spring:
  application:
    name: xypai-location
  cloud:
    nacos:
      server-addr: @nacos.server@
      discovery:
        namespace: @nacos.namespace@
        group: @nacos.discovery.group@
      config:
        namespace: @nacos.namespace@
        group: @nacos.config.group@
        file-extension: yml
  config:
    import:
      - optional:nacos:application-common.yml
      - optional:nacos:datasource.yml
      - optional:nacos:xypai-location-${spring.profiles.active}.yml
```

---

### 2. ruoyi-api-location (API模块)

**路径**: `RuoYi-Cloud-Plus/ruoyi-api/ruoyi-api-location/`

```java
// src/main/java/org/dromara/location/api/RemoteLocationService.java
package org.dromara.location.api;

import org.dromara.common.core.domain.R;
import org.dromara.location.api.domain.DistanceVo;
import org.dromara.location.api.domain.LocationPointDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 位置服务远程调用接口
 */
public interface RemoteLocationService {

    /**
     * 计算两点距离
     */
    R<DistanceVo> calculateDistance(BigDecimal fromLat, BigDecimal fromLng,
                                    BigDecimal toLat, BigDecimal toLng);

    /**
     * 批量计算距离
     */
    R<List<DistanceVo>> calculateBatchDistance(BigDecimal fromLat, BigDecimal fromLng,
                                               List<LocationPointDto> targets);

    /**
     * 获取城市信息
     */
    R<CityInfoVo> getCityInfo(String cityCode);

    /**
     * 验证坐标有效性
     */
    R<Boolean> validateCoordinates(BigDecimal latitude, BigDecimal longitude);
}
```

```java
// src/main/java/org/dromara/location/api/domain/DistanceVo.java
@Data
public class DistanceVo implements Serializable {
    private Long id;                // 目标ID
    private BigDecimal distance;    // 距离
    private String unit;            // 单位
    private String displayText;     // 显示文本
}
```

---

### 3. 数据库SQL脚本

**路径**: `script/sql/xypai_location.sql`

```sql
-- ========================================
-- XiangYuPai Location Service Database
-- 享遇派位置服务数据库
-- ========================================

CREATE DATABASE IF NOT EXISTS `xypai_location` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `xypai_location`;

-- ========================================
-- 1. 地点表
-- ========================================
CREATE TABLE `location` (
    `id`            BIGINT(20)      NOT NULL COMMENT '地点ID',
    `name`          VARCHAR(100)    NOT NULL COMMENT '地点名称',
    `address`       VARCHAR(255)    NOT NULL COMMENT '详细地址',
    `latitude`      DECIMAL(10,6)   NOT NULL COMMENT '纬度',
    `longitude`     DECIMAL(10,6)   NOT NULL COMMENT '经度',
    `location_point` POINT          NOT NULL SRID 4326 COMMENT '地理坐标点(空间索引)',
    `geohash`       VARCHAR(20)     COMMENT 'Geohash编码',

    `category`      VARCHAR(50)     COMMENT '分类',
    `province`      VARCHAR(50)     COMMENT '省份',
    `city_code`     VARCHAR(10)     COMMENT '城市代码',
    `city`          VARCHAR(50)     COMMENT '城市',
    `district`      VARCHAR(50)     COMMENT '区县',
    `street`        VARCHAR(50)     COMMENT '街道',

    `source`        VARCHAR(20)     COMMENT '数据来源',
    `extra_info`    TEXT            COMMENT '额外信息JSON',

    `status`        TINYINT(1)      DEFAULT 0 COMMENT '状态: 0=正常,1=禁用',
    `created_at`    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `created_by`    BIGINT(20)      COMMENT '创建人',
    `updated_at`    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by`    BIGINT(20)      COMMENT '更新人',
    `deleted`       BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`       INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    SPATIAL KEY `idx_location_point` (`location_point`),
    KEY `idx_geohash` (`geohash`),
    KEY `idx_city_code` (`city_code`),
    KEY `idx_category` (`category`),
    KEY `idx_deleted` (`deleted`),
    FULLTEXT KEY `idx_fulltext` (`name`, `address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点表';

-- ========================================
-- 2. 城市表
-- ========================================
CREATE TABLE `city` (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `city_code`         VARCHAR(10)     UNIQUE NOT NULL COMMENT '城市代码',
    `city_name`         VARCHAR(50)     NOT NULL COMMENT '城市名称',
    `province`          VARCHAR(50)     COMMENT '省份',
    `pinyin`            VARCHAR(50)     COMMENT '拼音',
    `first_letter`      CHAR(1)         COMMENT '首字母',

    `center_lat`        DECIMAL(10,6)   COMMENT '中心点纬度',
    `center_lng`        DECIMAL(10,6)   COMMENT '中心点经度',
    `center_point`      POINT           SRID 4326 COMMENT '中心点坐标',
    `city_boundary`     POLYGON         COMMENT '城市边界多边形',

    `is_hot`            TINYINT(1)      DEFAULT 0 COMMENT '是否热门: 0=否,1=是',
    `has_districts`     TINYINT(1)      DEFAULT 0 COMMENT '是否有区域: 0=否,1=是',
    `sort_order`        INT(11)         DEFAULT 0 COMMENT '排序',

    `status`            TINYINT(1)      DEFAULT 1 COMMENT '状态: 0=禁用,1=正常',
    `created_at`        DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `created_by`        BIGINT(20),
    `updated_at`        DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by`        BIGINT(20),
    `deleted`           BIGINT(20)      DEFAULT 0 COMMENT '逻辑删除',
    `version`           INT(11)         DEFAULT 0 COMMENT '乐观锁版本',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_code` (`city_code`, `deleted`),
    SPATIAL KEY `idx_center_point` (`center_point`),
    SPATIAL KEY `idx_city_boundary` (`city_boundary`),
    KEY `idx_first_letter` (`first_letter`),
    KEY `idx_is_hot` (`is_hot`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市表';

-- ========================================
-- 3. 初始化热门城市数据
-- ========================================
INSERT INTO `city` (`city_code`, `city_name`, `province`, `pinyin`, `first_letter`,
                    `center_lat`, `center_lng`, `is_hot`, `sort_order`, `status`)
VALUES
('110100', '北京', '北京市', 'beijing', 'B', 39.904989, 116.405285, 1, 1, 1),
('310100', '上海', '上海市', 'shanghai', 'S', 31.230416, 121.473701, 1, 2, 1),
('440100', '广州', '广东省', 'guangzhou', 'G', 23.129110, 113.264385, 1, 3, 1),
('440300', '深圳', '广东省', 'shenzhen', 'S', 22.543099, 114.057868, 1, 4, 1),
('330100', '杭州', '浙江省', 'hangzhou', 'H', 30.274084, 120.155070, 1, 5, 1),
('510100', '成都', '四川省', 'chengdu', 'C', 30.572815, 104.066801, 1, 6, 1),
('420100', '武汉', '湖北省', 'wuhan', 'W', 30.592849, 114.305539, 1, 7, 1),
('320100', '南京', '江苏省', 'nanjing', 'N', 32.041546, 118.767413, 1, 8, 1);
```

---

## 下一步实施计划

### Phase 1: 完成 xypai-location (优先级:高)
1. [ ] 完成 Controller 层
   - LocationController.java (C端API)
   - CityController.java (C端API)
   - CityManageController.java (B端管理)
2. [ ] 完成 CityServiceImpl
3. [ ] 创建 Dubbo RPC 实现
4. [ ] 创建配置文件 (application.yml, bootstrap.yml)
5. [ ] 创建 MyBatis XML 映射文件
6. [ ] 创建 ruoyi-api-location 模块
7. [ ] 运行数据库脚本初始化数据

### Phase 2: 实现 xypai-media (优先级:高)
1. [ ] 创建模块结构
2. [ ] 实现文件上传逻辑(OSS集成)
3. [ ] 实现图片压缩(Sharp/ImageMagick)
4. [ ] 实现视频处理(FFmpeg)
5. [ ] 创建 Dubbo RPC 接口

### Phase 3: 实现 xypai-notification (优先级:中)
1. [ ] 创建模块结构
2. [ ] 实现通知CRUD
3. [ ] 实现未读数统计
4. [ ] 实现WebSocket推送
5. [ ] 创建 Dubbo RPC 接口

### Phase 4: 实现 xypai-report (优先级:中)
1. [ ] 创建模块结构
2. [ ] 实现举报提交
3. [ ] 实现审核流程
4. [ ] 实现处罚机制
5. [ ] 创建 Dubbo RPC 接口

### Phase 5: 集成测试 (优先级:高)
1. [ ] 单元测试
2. [ ] 集成测试
3. [ ] 性能测试
4. [ ] 文档更新

---

## 快速参考

### 标准文件模板路径
- 参考模块: `ruoyi-example-common/ruoyi-demo`
- Controller 模板: `TestDemoController.java`
- Service 模板: `TestDemoServiceImpl.java`
- Mapper 模板: `TestDemoMapper.java`
- Entity 模板: `TestDemo.java`

### 命名规范
- Entity: `{Entity}.java`
- BO: `{Entity}{Action}Bo.java`
- VO: `{Entity}{Type}Vo.java`
- Controller: `{Entity}Controller.java`
- Service: `I{Entity}Service.java`
- ServiceImpl: `{Entity}ServiceImpl.java`
- Mapper: `{Entity}Mapper.java`

### 关键注解
- `@TableName` - MyBatis-Plus表映射
- `@TableId(type = IdType.ASSIGN_ID)` - 主键(雪花ID)
- `@TableLogic` - 逻辑删除
- `@Version` - 乐观锁
- `@AutoMapper` - 自动映射(MapStruct)
- `@DubboService` - Dubbo服务提供者
- `@DubboReference` - Dubbo服务消费者
- `@SaCheckRole` / `@SaCheckPermission` - 权限控制

---

## 总结

### 已完成
✅ xypai-common 父模块创建
✅ xypai-location 核心代码框架(60%完成度)
  - Entity层完成
  - VO层完成
  - BO层部分完成
  - Mapper接口完成
  - Service核心实现完成
  - 距离计算算法完成

### 待完成
⏳ xypai-location Controller层
⏳ xypai-location Dubbo RPC实现
⏳ xypai-location 配置文件
⏳ ruoyi-api-location API模块
⏳ 数据库脚本
⏳ xypai-media 整个模块
⏳ xypai-notification 整个模块
⏳ xypai-report 整个模块

**预计工作量**: 2-3天完整实现所有模块

---

**文档版本**: v1.0
**创建日期**: 2025-11-14
**作者**: XiangYuPai Team
