# 💻 用户模块v7.1代码使用示例

> **目标读者**: 后端开发、前端开发  
> **更新日期**: 2025-01-14

---

## 📋 目录

1. [用户统计系统](#用户统计系统)
2. [职业标签系统](#职业标签系统)
3. [资料完整度计算](#资料完整度计算)
4. [登录安全增强](#登录安全增强)
5. [钱包交易系统](#钱包交易系统)
6. [用户关系管理](#用户关系管理)

---

## 1. 用户统计系统

### 场景1: 用户关注操作

```java
@Service
@RequiredArgsConstructor
public class UserFollowService {
    
    private final IUserStatsService userStatsService;
    private final IUserRelationService userRelationService;
    
    /**
     * 用户A关注用户B
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(Long userIdA, Long userIdB) {
        // 1. 创建关注关系
        UserRelation relation = UserRelation.builder()
            .userId(userIdA)
            .targetId(userIdB)
            .type(UserRelation.Type.FOLLOW.getCode())
            .status(1)
            .build();
        userRelationService.save(relation);
        
        // 2. 更新统计数据（Redis双写）
        userStatsService.incrementFollowerCount(userIdB);  // B的粉丝+1
        userStatsService.incrementFollowingCount(userIdA); // A的关注+1
        
        log.info("用户关注成功，A: {}, B: {}", userIdA, userIdB);
        return true;
    }
    
    /**
     * 用户A取消关注用户B
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowUser(Long userIdA, Long userIdB) {
        // 1. 取消关注关系
        userRelationService.unfollowUser(userIdA, userIdB);
        
        // 2. 更新统计数据
        userStatsService.decrementFollowerCount(userIdB);  // B的粉丝-1
        userStatsService.decrementFollowingCount(userIdA); // A的关注-1
        
        return true;
    }
}
```

### 场景2: 查询用户统计（优先Redis）

```java
@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private final IUserStatsService userStatsService;
    
    /**
     * 获取用户主页数据
     */
    public UserHomePageVO getUserHomePage(Long userId) {
        // 查询统计（优先从Redis读取，性能提升10倍）
        UserStatsVO stats = userStatsService.getUserStats(userId);
        
        // 组装主页VO
        UserHomePageVO vo = UserHomePageVO.builder()
            .userId(userId)
            .followerCount(stats.getFollowerCount())
            .followingCount(stats.getFollowingCount())
            .contentCount(stats.getContentCount())
            .totalLikeCount(stats.getTotalLikeCount())
            .isPopular(stats.getIsPopular())
            .isActive(stats.getIsActive())
            .build();
        
        return vo;
    }
}
```

### 场景3: 批量查询统计

```java
/**
 * 批量查询用户统计（优化版）
 */
public List<UserCardVO> getUserCards(List<Long> userIds) {
    // 批量查询统计（一次查询）
    List<UserStatsVO> statsList = userStatsService.getBatchUserStats(userIds);
    
    // 转换为Map
    Map<Long, UserStatsVO> statsMap = statsList.stream()
        .collect(Collectors.toMap(UserStatsVO::getUserId, s -> s));
    
    // 组装用户卡片
    return userIds.stream()
        .map(userId -> {
            UserStatsVO stats = statsMap.get(userId);
            return UserCardVO.builder()
                .userId(userId)
                .followerCount(stats.getFollowerCount())
                .contentCount(stats.getContentCount())
                .build();
        })
        .collect(Collectors.toList());
}
```

---

## 2. 职业标签系统

### 场景1: 用户选择职业

```java
@Service
@RequiredArgsConstructor
public class UserOccupationService {
    
    private final IOccupationService occupationService;
    
    /**
     * 用户编辑资料 - 选择职业标签
     */
    public boolean updateUserOccupations(Long userId, List<String> occupationCodes) {
        // 构建DTO
        UserOccupationUpdateDTO updateDTO = UserOccupationUpdateDTO.builder()
            .occupationCodes(occupationCodes)
            .keepSortOrder(false)  // 按提交顺序排序
            .build();
        
        // 批量更新（最多5个）
        return occupationService.updateUserOccupations(userId, updateDTO);
    }
}
```

### 场景2: 职业筛选查询

```java
/**
 * 筛选模特用户
 */
public List<Long> searchModelUsers() {
    // 查询所有模特用户ID
    List<Long> userIds = occupationService.getUserIdsByOccupation("model");
    
    // 进一步筛选（在线、认证）
    return userIds.stream()
        .filter(userId -> {
            // 检查用户状态
            UserProfile profile = userProfileService.getById(userId);
            return profile.isOnline() && profile.getIsRealVerified();
        })
        .collect(Collectors.toList());
}
```

### 场景3: 展示用户职业

```java
/**
 * 用户卡片 - 展示职业标签
 */
public UserCardVO buildUserCard(Long userId) {
    // 查询职业标签
    List<UserOccupationVO> occupations = occupationService.getUserOccupations(userId);
    
    // 只展示前3个
    List<String> occupationNames = occupations.stream()
        .limit(3)
        .map(UserOccupationVO::getOccupationName)
        .collect(Collectors.toList());
    
    return UserCardVO.builder()
        .userId(userId)
        .occupationTags(occupationNames)  // ["模特", "学生", "设计师"]
        .build();
}
```

---

## 3. 资料完整度计算

### 场景1: 用户编辑资料后计算

```java
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {
    
    private final IOccupationService occupationService;
    
    /**
     * 更新用户资料并计算完整度
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        // 1. 更新资料
        UserProfileNew profile = convertToEntity(updateDTO);
        profile.setUserId(userId);
        
        // 2. 查询职业标签
        List<UserOccupation> occupations = 
            userOccupationMapper.selectByUserId(userId);
        
        // 3. 计算完整度
        int score = ProfileCompletenessCalculator.calculate(profile, occupations);
        profile.setProfileCompleteness(score);
        
        // 4. 更新编辑时间
        profile.markAsEdited();
        
        // 5. 保存
        int result = userProfileMapper.updateById(profile);
        
        log.info("更新资料成功，userId: {}, 完整度: {}%", userId, score);
        return result > 0;
    }
}
```

### 场景2: 提示用户完善资料

```java
/**
 * 获取资料完善建议
 */
public ProfileSuggestionsVO getProfileSuggestions(Long userId) {
    // 查询资料
    UserProfileNew profile = userProfileMapper.selectById(userId);
    List<UserOccupation> occupations = userOccupationMapper.selectByUserId(userId);
    
    // 计算完整度
    int score = ProfileCompletenessCalculator.calculate(profile, occupations);
    
    // 获取建议
    List<String> suggestions = ProfileCompletenessCalculator.getSuggestions(profile, occupations);
    
    return ProfileSuggestionsVO.builder()
        .currentScore(score)
        .level(ProfileCompletenessCalculator.getCompletenessLevel(score))
        .isComplete(ProfileCompletenessCalculator.isComplete(score))
        .suggestions(suggestions)  // ["设置昵称", "上传头像", "完成实名认证（+15分）"]
        .build();
}
```

### 场景3: 推荐算法应用

```java
/**
 * 推荐用户列表（优先推荐资料完整的用户）
 */
public List<UserRecommendVO> getRecommendUsers(int limit) {
    // 查询候选用户
    List<UserProfileNew> profiles = userProfileMapper.selectList(
        new LambdaQueryWrapper<UserProfileNew>()
            .ge(UserProfileNew::getProfileCompleteness, 80)  // 完整度≥80%
            .eq(UserProfileNew::getOnlineStatus, 1)           // 在线
            .orderByDesc(UserProfileNew::getProfileCompleteness)
            .last("LIMIT " + limit)
    );
    
    return profiles.stream()
        .map(this::convertToRecommendVO)
        .collect(Collectors.toList());
}
```

---

## 4. 登录安全增强

### 场景1: 登录失败处理

```java
@Service
@RequiredArgsConstructor
public class LoginSecurityService {
    
    private final UserMapper userMapper;
    
    /**
     * 处理登录失败
     */
    public void handleLoginFailed(Long userId, String ip, String deviceId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        // 增加失败次数
        int failCount = user.incrementLoginFailCount();
        
        // 达到5次，锁定账户30分钟
        if (failCount >= 5) {
            user.lockAccount(30);
            log.warn("账户已锁定，userId: {}, IP: {}", userId, ip);
        }
        
        // 更新数据库
        userMapper.updateById(user);
    }
    
    /**
     * 处理登录成功
     */
    public void handleLoginSuccess(Long userId, String ip, String deviceId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        // 重置失败次数
        user.resetLoginFailCount();
        
        // 更新最后登录信息
        user.updateLastLogin(ip, deviceId);
        
        // 更新数据库
        userMapper.updateById(user);
        
        log.info("登录成功，userId: {}, IP: {}", userId, ip);
    }
}
```

### 场景2: 检查账户锁定

```java
/**
 * 登录前检查
 */
public boolean canLogin(User user) {
    // 检查账户状态
    if (!user.isNormal()) {
        throw new ServiceException("账户已被禁用或冻结");
    }
    
    // 检查是否锁定
    if (user.isLocked()) {
        throw new ServiceException("账户已锁定，请稍后再试");
    }
    
    // 检查密码是否过期
    if (user.needPasswordUpdate()) {
        throw new ServiceException("密码已过期，请修改密码");
    }
    
    return true;
}
```

---

## 5. 钱包交易系统

### 场景1: 充值操作

```java
@Service
@RequiredArgsConstructor
public class WalletRechargeService {
    
    private final UserWalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
    
    /**
     * 用户充值
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, Long amount, String paymentMethod, String paymentNo) {
        // 1. 查询钱包
        UserWallet wallet = walletMapper.selectById(userId);
        
        // 2. 增加余额
        wallet.addBalance(amount);
        wallet.addIncome(amount);  // 累计收入
        
        // 3. 更新钱包（乐观锁）
        int result = walletMapper.updateById(wallet);
        if (result == 0) {
            throw new ServiceException("钱包更新失败，请重试");
        }
        
        // 4. 创建交易记录
        Transaction transaction = Transaction.builder()
            .userId(userId)
            .amount(amount)
            .type(Transaction.Type.RECHARGE.getCode())
            .status(Transaction.Status.SUCCESS.getCode())
            .paymentMethod(paymentMethod)
            .paymentNo(paymentNo)
            .description("账户充值")
            .build();
        transactionMapper.insert(transaction);
        
        log.info("充值成功，userId: {}, amount: {}分", userId, amount);
        return true;
    }
}
```

### 场景2: 下单扣款（冻结余额）

```java
/**
 * 下单冻结余额
 */
@Transactional(rollbackFor = Exception.class)
public boolean freezeBalanceForOrder(Long userId, Long orderId, Long amount) {
    // 1. 查询钱包
    UserWallet wallet = walletMapper.selectById(userId);
    
    // 2. 检查余额
    if (!wallet.hasEnoughBalance(amount)) {
        throw new ServiceException("余额不足");
    }
    
    // 3. 冻结余额
    boolean frozen = wallet.freezeBalance(amount);
    if (!frozen) {
        throw new ServiceException("冻结余额失败");
    }
    
    // 4. 更新钱包
    walletMapper.updateById(wallet);
    
    // 5. 创建交易记录
    Transaction transaction = Transaction.builder()
        .userId(userId)
        .amount(-amount)  // 负数表示支出
        .type(Transaction.Type.CONSUME.getCode())
        .status(Transaction.Status.PROCESSING.getCode())
        .build();
    transaction.setOrderRef(orderId);
    transactionMapper.insert(transaction);
    
    log.info("冻结余额成功，userId: {}, orderId: {}, amount: {}", userId, orderId, amount);
    return true;
}
```

### 场景3: 订单完成（解冻扣款）

```java
/**
 * 订单完成，实际扣款
 */
@Transactional(rollbackFor = Exception.class)
public boolean completeOrder(Long userId, Long orderId, Long amount) {
    // 1. 查询钱包
    UserWallet wallet = walletMapper.selectById(userId);
    
    // 2. 检查冻结金额
    if (!wallet.hasEnoughFrozen(amount)) {
        throw new ServiceException("冻结金额不足");
    }
    
    // 3. 扣除冻结金额（不需要解冻，直接扣除）
    wallet.setFrozen(wallet.getFrozen() - amount);
    wallet.addExpense(amount);  // 累计支出
    
    // 4. 更新钱包
    walletMapper.updateById(wallet);
    
    // 5. 更新交易状态
    transactionMapper.update(null, 
        new LambdaUpdateWrapper<Transaction>()
            .set(Transaction::getStatus, Transaction.Status.SUCCESS.getCode())
            .eq(Transaction::getRefType, "order")
            .eq(Transaction::getRefId, orderId)
    );
    
    return true;
}
```

---

## 6. 用户关系管理

### 场景1: 互相关注检测

```java
/**
 * 检查是否互相关注
 */
public boolean isMutualFollow(Long userIdA, Long userIdB) {
    // A关注B
    boolean aFollowsB = userRelationService.hasRelation(
        userIdA, userIdB, UserRelation.Type.FOLLOW.getCode()
    );
    
    // B关注A
    boolean bFollowsA = userRelationService.hasRelation(
        userIdB, userIdA, UserRelation.Type.FOLLOW.getCode()
    );
    
    return aFollowsB && bFollowsA;
}
```

### 场景2: 获取粉丝列表

```java
/**
 * 获取用户的粉丝列表
 */
public List<UserListVO> getFollowers(Long userId, int page, int size) {
    // 查询关注该用户的所有人
    List<UserRelation> relations = userRelationMapper.selectList(
        new LambdaQueryWrapper<UserRelation>()
            .eq(UserRelation::getTargetId, userId)
            .eq(UserRelation::getType, UserRelation.Type.FOLLOW.getCode())
            .eq(UserRelation::getStatus, 1)
            .orderByDesc(UserRelation::getCreatedAt)
    );
    
    // 提取用户ID
    List<Long> followerIds = relations.stream()
        .map(UserRelation::getUserId)
        .collect(Collectors.toList());
    
    // 批量查询用户信息
    return userService.getBatchUsers(followerIds);
}
```

---

## 🌐 前端调用示例

### Vue 3 + Axios

#### 1. 查询用户统计
```javascript
// api/userStats.js
import axios from 'axios';

export const getUserStats = (userId) => {
  return axios.get(`/api/v1/users/stats/${userId}`);
};

// 使用
const { data } = await getUserStats(123);
console.log('粉丝数:', data.data.followerCount);
```

#### 2. 更新用户职业
```javascript
// api/occupation.js
export const updateUserOccupations = (userId, occupationCodes) => {
  return axios.put(`/api/v1/occupation/user/${userId}`, {
    occupationCodes,
    keepSortOrder: false
  });
};

// 使用
await updateUserOccupations(123, ['model', 'student', 'designer']);
```

#### 3. 查询职业列表
```javascript
// api/occupation.js
export const getAllOccupations = () => {
  return axios.get('/api/v1/occupation/list');
};

// 使用（分组展示）
const { data } = await getAllOccupations();
const groupedByCategory = data.data.reduce((acc, occupation) => {
  const category = occupation.category;
  if (!acc[category]) acc[category] = [];
  acc[category].push(occupation);
  return acc;
}, {});

console.log(groupedByCategory);
// {
//   "艺术": [{code: "model", name: "模特"}, ...],
//   "教育": [{code: "student", name: "学生"}, ...]
// }
```

#### 4. 资料完整度进度条
```vue
<template>
  <div class="profile-completeness">
    <el-progress 
      :percentage="profileScore" 
      :color="getProgressColor(profileScore)"
    />
    <p>资料完整度: {{ profileScore }}% ({{ levelDesc }})</p>
    
    <!-- 完善建议 -->
    <div v-if="!isComplete" class="suggestions">
      <p>完善以下内容可提升排名：</p>
      <ul>
        <li v-for="(suggestion, index) in suggestions" :key="index">
          {{ suggestion }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getUserProfile } from '@/api/user';

const profileScore = ref(0);
const suggestions = ref([]);

const isComplete = computed(() => profileScore.value >= 80);
const levelDesc = computed(() => {
  if (profileScore.value >= 90) return '优秀';
  if (profileScore.value >= 80) return '良好';
  if (profileScore.value >= 60) return '一般';
  return '需完善';
});

const getProgressColor = (score) => {
  if (score >= 80) return '#67C23A';  // 绿色
  if (score >= 60) return '#E6A23C';  // 橙色
  return '#F56C6C';  // 红色
};

// 加载资料
const loadProfile = async () => {
  const { data } = await getUserProfile();
  profileScore.value = data.profileCompleteness;
  // suggestions从后端获取
};
</script>
```

---

## 🧪 Postman测试集合

### 测试集合JSON
```json
{
  "info": {
    "name": "用户模块v7.1测试",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. 初始化用户统计",
      "request": {
        "method": "POST",
        "url": "http://localhost:9401/api/v1/users/stats/init?userId=1"
      }
    },
    {
      "name": "2. 查询用户统计",
      "request": {
        "method": "GET",
        "url": "http://localhost:9401/api/v1/users/stats/1"
      }
    },
    {
      "name": "3. 查询所有职业",
      "request": {
        "method": "GET",
        "url": "http://localhost:9401/api/v1/occupation/list"
      }
    },
    {
      "name": "4. 更新用户职业",
      "request": {
        "method": "PUT",
        "url": "http://localhost:9401/api/v1/occupation/user/1",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"occupationCodes\": [\"model\", \"student\", \"designer\"]\n}"
        }
      }
    },
    {
      "name": "5. 人气用户排行",
      "request": {
        "method": "GET",
        "url": "http://localhost:9401/api/v1/users/stats/popular?limit=10"
      }
    }
  ]
}
```

---

## 🔧 常见问题解决

### Q1: Redis缓存不生效？

```java
// 手动刷新缓存
userStatsService.refreshCache(userId);

// 检查Redis Key
redis-cli> KEYS user:stats:*
redis-cli> HGETALL user:stats:1
```

### Q2: 统计数据不准确？

```java
// 强制同步MySQL
UserStats stats = userStatsMapper.selectById(userId);
userStatsService.refreshCache(userId);

// 对比差异
UserStatsVO cacheStats = userStatsService.getUserStats(userId);
```

### Q3: 职业更新失败？

```java
// 检查职业编码是否存在
OccupationDict occupation = occupationDictMapper.selectById("model");
if (occupation == null) {
    log.error("职业编码不存在");
}

// 检查数量限制
int count = userOccupationMapper.countByUserId(userId);
if (count >= 5) {
    log.error("职业数量已达上限");
}
```

---

## 📚 扩展阅读

- [README.md](README.md) - 模块概览
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API详细文档
- [QUICK_START.md](QUICK_START.md) - 快速开始指南

---

**代码示例完整，开箱即用！** 🎉

