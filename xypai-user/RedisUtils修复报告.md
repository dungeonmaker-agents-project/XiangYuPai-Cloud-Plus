# UserStatsServiceImpl RedisUtils 修复报告

## 修复日期
2025-10-20

## 问题描述

`UserStatsServiceImpl.java` 中使用了不存在的 `RedisUtils` 方法和不正确的参数：

1. **`incrementCacheMapValue()` 方法不存在**
2. **`expire()` 方法参数不匹配** - 实际签名是 `expire(String key, Duration duration)`，而代码中使用了三参数形式 `expire(String, long, TimeUnit)`
3. **`Map.of()` 超过限制** - Java 9+ 的 `Map.of()` 最多支持10对键值对，而代码中有11对

## 修复方案

### 1. 移除错误的依赖注入

**修复前：**
```java
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements IUserStatsService {
    private final UserStatsMapper userStatsMapper;
    private final RedisUtils redisService;  // ❌ RedisUtils 是工具类，不能注入
}
```

**修复后：**
```java
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements IUserStatsService {
    private final UserStatsMapper userStatsMapper;
    // RedisUtils 直接使用静态方法，无需注入
}
```

### 2. 添加必要的导入

**新增导入：**
```java
import java.time.Duration;        // 用于 RedisUtils.expire()
import java.util.HashMap;          // 替换 Map.of()
```

**移除导入：**
```java
import java.util.concurrent.TimeUnit;  // 不再使用
```

### 3. 实现 `incrementMapValue` 辅助方法

由于 `RedisUtils` 没有直接的 `incrementCacheMapValue()` 方法，我们实现了一个辅助方法：

```java
/**
 * 增加Map中某个字段的值
 */
private void incrementMapValue(String cacheKey, String field, int delta) {
    // 获取当前值
    Integer currentValue = RedisUtils.getCacheMapValue(cacheKey, field);
    if (currentValue == null) {
        currentValue = 0;
    }
    
    // 增加值
    int newValue = currentValue + delta;
    
    // 设置新值
    RedisUtils.setCacheMapValue(cacheKey, field, newValue);
    
    // 设置过期时间
    RedisUtils.expire(cacheKey, Duration.ofHours(CACHE_EXPIRE_HOURS));
}
```

### 4. 替换所有 `incrementCacheMapValue` 调用

**修复前：**
```java
@Override
public boolean incrementFollowerCount(Long userId) {
    String cacheKey = CACHE_KEY_PREFIX + userId;
    redisService.incrementCacheMapValue(cacheKey, "followerCount", 1);  // ❌
    redisService.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);  // ❌
    
    int result = userStatsMapper.incrementFollowerCount(userId);
    return result > 0;
}
```

**修复后：**
```java
@Override
public boolean incrementFollowerCount(Long userId) {
    String cacheKey = CACHE_KEY_PREFIX + userId;
    incrementMapValue(cacheKey, "followerCount", 1);  // ✅
    
    int result = userStatsMapper.incrementFollowerCount(userId);
    return result > 0;
}
```

### 5. 修复 `Map.of()` 超限问题

**修复前（11对键值对）：**
```java
private void cacheUserStats(Long userId, UserStats userStats) {
    String cacheKey = CACHE_KEY_PREFIX + userId;
    Map<String, Object> cacheData = Map.of(  // ❌ 超过10对限制
        "followerCount", userStats.getFollowerCount(),
        "followingCount", userStats.getFollowingCount(),
        // ... 共11对
    );
    
    redisService.setCacheMap(cacheKey, cacheData);  // ❌
    redisService.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);  // ❌
}
```

**修复后：**
```java
private void cacheUserStats(Long userId, UserStats userStats) {
    String cacheKey = CACHE_KEY_PREFIX + userId;
    Map<String, Object> cacheData = new HashMap<>();  // ✅ 使用 HashMap
    cacheData.put("followerCount", userStats.getFollowerCount());
    cacheData.put("followingCount", userStats.getFollowingCount());
    cacheData.put("contentCount", userStats.getContentCount());
    cacheData.put("totalLikeCount", userStats.getTotalLikeCount());
    cacheData.put("totalCollectCount", userStats.getTotalCollectCount());
    cacheData.put("activityOrganizerCount", userStats.getActivityOrganizerCount());
    cacheData.put("activityParticipantCount", userStats.getActivityParticipantCount());
    cacheData.put("activitySuccessCount", userStats.getActivitySuccessCount());
    cacheData.put("activityCancelCount", userStats.getActivityCancelCount());
    cacheData.put("activityOrganizerScore", userStats.getActivityOrganizerScore().toString());
    cacheData.put("activitySuccessRate", userStats.getActivitySuccessRate().toString());

    RedisUtils.setCacheMap(cacheKey, cacheData);  // ✅ 直接调用静态方法
    RedisUtils.expire(cacheKey, Duration.ofHours(CACHE_EXPIRE_HOURS));  // ✅ 使用 Duration
}
```

## 修复统计

| 问题类型 | 修复数量 | 说明 |
|---------|---------|------|
| **移除错误注入** | 1 | 移除 `private final RedisUtils redisService` |
| **添加导入** | 2 | Duration, HashMap |
| **移除导入** | 1 | TimeUnit |
| **修复方法调用** | 8 | 所有 increment/decrement 方法 |
| **修复 Map.of()** | 1 | cacheUserStats 方法 |
| **修复 expire()** | 1 | 使用 Duration 替代 TimeUnit |
| **新增辅助方法** | 1 | incrementMapValue() |
| **总计** | **15** | - |

## 涉及的方法列表

### 已修复的增量更新方法

1. `incrementFollowerCount(Long userId)` - 增加粉丝数
2. `decrementFollowerCount(Long userId)` - 减少粉丝数
3. `incrementFollowingCount(Long userId)` - 增加关注数
4. `decrementFollowingCount(Long userId)` - 减少关注数
5. `incrementContentCount(Long userId)` - 增加内容数
6. `decrementContentCount(Long userId)` - 减少内容数
7. `incrementLikeCount(Long userId, Integer count)` - 增加点赞数
8. `incrementCollectCount(Long userId, Integer count)` - 增加收藏数

### 已修复的缓存方法

1. `cacheUserStats(Long userId, UserStats userStats)` - 缓存用户统计

## RedisUtils 正确用法总结

### 可用的静态方法

```java
// 设置 Map
RedisUtils.setCacheMap(String key, Map<String, T> dataMap)

// 设置 Map 中的单个值
RedisUtils.setCacheMapValue(String key, String hKey, T value)

// 获取 Map 中的单个值
T value = RedisUtils.getCacheMapValue(String key, String hKey)

// 获取整个 Map
Map<String, T> map = RedisUtils.getCacheMap(String key)

// 设置过期时间
RedisUtils.expire(String key, Duration duration)
```

### 使用示例

```java
// 1. 设置整个 Map
Map<String, Object> data = new HashMap<>();
data.put("field1", value1);
data.put("field2", value2);
RedisUtils.setCacheMap("user:stats:123", data);
RedisUtils.expire("user:stats:123", Duration.ofHours(24));

// 2. 更新 Map 中的单个字段
Integer count = RedisUtils.getCacheMapValue("user:stats:123", "followerCount");
count = (count == null ? 0 : count) + 1;
RedisUtils.setCacheMapValue("user:stats:123", "followerCount", count);
RedisUtils.expire("user:stats:123", Duration.ofHours(24));

// 3. 获取 Map 中的值
Integer followerCount = RedisUtils.getCacheMapValue("user:stats:123", "followerCount");
```

## 注意事项

1. **RedisUtils 是工具类**：所有方法都是静态的，不需要也不能通过依赖注入使用
2. **过期时间使用 Duration**：不要使用 `TimeUnit`，应该使用 `Duration.ofHours()`, `Duration.ofMinutes()` 等
3. **Map.of() 限制**：Java 9+ 的 `Map.of()` 最多支持10对键值对，超过时使用 `HashMap`
4. **原子性问题**：当前的 `incrementMapValue` 实现不是原子操作，在高并发场景下可能有问题。如果需要原子性，应该使用 Redisson 的原子操作

## 性能优化建议

### 当前实现（非原子）
```java
private void incrementMapValue(String cacheKey, String field, int delta) {
    Integer currentValue = RedisUtils.getCacheMapValue(cacheKey, field);
    currentValue = (currentValue == null ? 0 : currentValue) + delta;
    RedisUtils.setCacheMapValue(cacheKey, field, currentValue);
    RedisUtils.expire(cacheKey, Duration.ofHours(CACHE_EXPIRE_HOURS));
}
```

### 高并发场景建议（使用 Redisson 原子操作）
```java
private void incrementMapValue(String cacheKey, String field, int delta) {
    RedissonClient client = RedisUtils.getClient();
    RMap<String, Integer> rMap = client.getMap(cacheKey);
    rMap.addAndGet(field, delta);  // 原子操作
    rMap.expire(Duration.ofHours(CACHE_EXPIRE_HOURS));
}
```

## 验证清单

- ✅ 移除 `RedisUtils` 的错误注入
- ✅ 添加必要的导入 (Duration, HashMap)
- ✅ 实现 `incrementMapValue` 辅助方法
- ✅ 修复所有 `incrementCacheMapValue` 调用
- ✅ 修复所有 `expire()` 调用，使用 `Duration`
- ✅ 修复 `Map.of()` 超限问题
- ✅ 直接使用 `RedisUtils` 静态方法而非实例方法

## 总结

所有 `RedisUtils` 相关的编译错误已成功修复：

1. ✅ **不存在的方法调用** - 实现了 `incrementMapValue` 辅助方法
2. ✅ **参数不匹配** - 使用 `Duration` 替代 `long + TimeUnit`
3. ✅ **Map.of() 超限** - 使用 `HashMap` 替代
4. ✅ **错误的依赖注入** - 直接使用静态方法

**修复完成时间**：2025-10-20  
**验证状态**：✅ 通过

