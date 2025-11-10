package com.xypai.user.constant;

/**
 * 用户模块常量
 *
 * @author Bob
 * @date 2025-01-14
 */
public class UserConstants {

    // ==================== Redis缓存Key前缀 ====================

    /**
     * 用户统计缓存Key前缀
     */
    public static final String CACHE_USER_STATS_PREFIX = "user:stats:";

    /**
     * 用户资料缓存Key前缀
     */
    public static final String CACHE_USER_PROFILE_PREFIX = "user:profile:";

    /**
     * 用户职业缓存Key前缀
     */
    public static final String CACHE_USER_OCCUPATION_PREFIX = "user:occupation:";

    /**
     * 职业字典缓存Key
     */
    public static final String CACHE_OCCUPATION_DICT = "occupation:dict:all";

    /**
     * 人气用户排行榜缓存Key
     */
    public static final String CACHE_POPULAR_USERS = "user:ranking:popular";

    /**
     * 优质组局者排行榜缓存Key
     */
    public static final String CACHE_QUALITY_ORGANIZERS = "user:ranking:organizers";

    // ==================== 缓存过期时间（秒） ====================

    /**
     * 用户统计缓存过期时间（1小时）
     */
    public static final long CACHE_USER_STATS_EXPIRE = 3600L;

    /**
     * 用户资料缓存过期时间（30分钟）
     */
    public static final long CACHE_USER_PROFILE_EXPIRE = 1800L;

    /**
     * 职业字典缓存过期时间（24小时）
     */
    public static final long CACHE_OCCUPATION_DICT_EXPIRE = 86400L;

    /**
     * 排行榜缓存过期时间（10分钟）
     */
    public static final long CACHE_RANKING_EXPIRE = 600L;

    // ==================== 业务限制常量 ====================

    /**
     * 用户职业标签最大数量
     */
    public static final int MAX_OCCUPATION_COUNT = 5;

    /**
     * 昵称最小长度
     */
    public static final int NICKNAME_MIN_LENGTH = 1;

    /**
     * 昵称最大长度
     */
    public static final int NICKNAME_MAX_LENGTH = 20;

    /**
     * 个人简介最大长度
     */
    public static final int BIO_MAX_LENGTH = 500;

    /**
     * 身高最小值（cm）
     */
    public static final int HEIGHT_MIN = 140;

    /**
     * 身高最大值（cm）
     */
    public static final int HEIGHT_MAX = 200;

    /**
     * 体重最小值（kg）
     */
    public static final int WEIGHT_MIN = 30;

    /**
     * 体重最大值（kg）
     */
    public static final int WEIGHT_MAX = 150;

    /**
     * 微信号最小长度
     */
    public static final int WECHAT_MIN_LENGTH = 6;

    /**
     * 微信号最大长度
     */
    public static final int WECHAT_MAX_LENGTH = 20;

    // ==================== 登录安全常量 ====================

    /**
     * 登录失败锁定次数
     */
    public static final int LOGIN_FAIL_LOCK_COUNT = 5;

    /**
     * 账户锁定时长（分钟）
     */
    public static final int ACCOUNT_LOCK_MINUTES = 30;

    /**
     * 密码过期天数
     */
    public static final int PASSWORD_EXPIRE_DAYS = 90;

    // ==================== 资料完整度常量 ====================

    /**
     * 资料完整度阈值（推荐算法）
     */
    public static final int PROFILE_COMPLETENESS_THRESHOLD = 80;

    /**
     * 核心字段总分
     */
    public static final int CORE_FIELDS_SCORE = 50;

    /**
     * 扩展字段总分
     */
    public static final int EXTENDED_FIELDS_SCORE = 50;

    /**
     * 实名认证加分
     */
    public static final int REAL_VERIFIED_BONUS = 15;

    // ==================== 统计数据常量 ====================

    /**
     * 活跃用户内容数阈值
     */
    public static final int ACTIVE_USER_CONTENT_THRESHOLD = 10;

    /**
     * 人气用户粉丝数阈值
     */
    public static final int POPULAR_USER_FOLLOWER_THRESHOLD = 1000;

    /**
     * 优质组局者评分阈值
     */
    public static final double QUALITY_ORGANIZER_SCORE_THRESHOLD = 4.5;

    /**
     * 优质组局者成功率阈值
     */
    public static final double QUALITY_ORGANIZER_SUCCESS_RATE_THRESHOLD = 80.0;

    // ==================== 用户状态枚举 ====================

    /**
     * 用户状态：禁用
     */
    public static final int USER_STATUS_DISABLED = 0;

    /**
     * 用户状态：正常
     */
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 用户状态：冻结
     */
    public static final int USER_STATUS_FROZEN = 2;

    /**
     * 用户状态：待激活
     */
    public static final int USER_STATUS_PENDING = 3;

    // ==================== 性别枚举 ====================

    /**
     * 性别：未设置
     */
    public static final int GENDER_UNSET = 0;

    /**
     * 性别：男
     */
    public static final int GENDER_MALE = 1;

    /**
     * 性别：女
     */
    public static final int GENDER_FEMALE = 2;

    /**
     * 性别：其他
     */
    public static final int GENDER_OTHER = 3;

    // ==================== 在线状态枚举 ====================

    /**
     * 在线状态：离线
     */
    public static final int ONLINE_STATUS_OFFLINE = 0;

    /**
     * 在线状态：在线
     */
    public static final int ONLINE_STATUS_ONLINE = 1;

    /**
     * 在线状态：忙碌
     */
    public static final int ONLINE_STATUS_BUSY = 2;

    /**
     * 在线状态：隐身
     */
    public static final int ONLINE_STATUS_INVISIBLE = 3;

    // ==================== 关系类型枚举 ====================

    /**
     * 关系类型：关注
     */
    public static final int RELATION_TYPE_FOLLOW = 1;

    /**
     * 关系类型：拉黑
     */
    public static final int RELATION_TYPE_BLOCK = 2;

    /**
     * 关系类型：好友
     */
    public static final int RELATION_TYPE_FRIEND = 3;

    /**
     * 关系类型：特别关注
     */
    public static final int RELATION_TYPE_SPECIAL_FOLLOW = 4;

    // ==================== 微信解锁条件 ====================

    /**
     * 微信解锁：公开
     */
    public static final int WECHAT_UNLOCK_PUBLIC = 0;

    /**
     * 微信解锁：关注后可见
     */
    public static final int WECHAT_UNLOCK_FOLLOW = 1;

    /**
     * 微信解锁：付费可见
     */
    public static final int WECHAT_UNLOCK_PAID = 2;

    /**
     * 微信解锁：私密
     */
    public static final int WECHAT_UNLOCK_PRIVATE = 3;

    // ==================== 默认值常量 ====================

    /**
     * 默认地区代码
     */
    public static final String DEFAULT_REGION_CODE = "+86";

    /**
     * 默认排序顺序
     */
    public static final int DEFAULT_SORT_ORDER = 0;

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 正则表达式 ====================

    /**
     * 手机号正则表达式
     */
    public static final String REGEX_MOBILE = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则表达式
     */
    public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * 用户名正则表达式（字母数字下划线中文）
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";

    /**
     * 微信号正则表达式（字母数字下划线中划线）
     */
    public static final String REGEX_WECHAT = "^[a-zA-Z0-9_-]+$";

    // ==================== 错误消息 ====================

    /**
     * 用户不存在
     */
    public static final String ERROR_USER_NOT_FOUND = "用户不存在";

    /**
     * 用户统计不存在
     */
    public static final String ERROR_USER_STATS_NOT_FOUND = "用户统计不存在";

    /**
     * 职业编码不存在
     */
    public static final String ERROR_OCCUPATION_NOT_FOUND = "职业编码不存在";

    /**
     * 职业数量超限
     */
    public static final String ERROR_OCCUPATION_LIMIT_EXCEEDED = "职业标签最多只能选择5个";

    /**
     * 账户已锁定
     */
    public static final String ERROR_ACCOUNT_LOCKED = "账户已锁定，请稍后再试";

    /**
     * 密码已过期
     */
    public static final String ERROR_PASSWORD_EXPIRED = "密码已过期，请修改密码";

    /**
     * 余额不足
     */
    public static final String ERROR_INSUFFICIENT_BALANCE = "余额不足";

    /**
     * 资料不完整
     */
    public static final String ERROR_PROFILE_INCOMPLETE = "资料完整度不足80%，请完善资料";
}

