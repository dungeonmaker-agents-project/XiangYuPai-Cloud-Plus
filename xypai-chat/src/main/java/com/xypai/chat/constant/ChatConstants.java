package com.xypai.chat.constant;

/**
 * 聊天模块常量类 (v7.1)
 * 
 * @author xypai
 * @date 2025-01-14
 */
public class ChatConstants {

    // ========== Redis Key前缀 ==========
    
    /**
     * 消息序列号Key前缀
     * 格式：chat:sequence:{conversationId}
     */
    public static final String REDIS_KEY_MESSAGE_SEQUENCE = "chat:sequence:";

    /**
     * 用户在线状态Key前缀
     * 格式：chat:online:{userId}
     */
    public static final String REDIS_KEY_USER_ONLINE = "chat:online:";

    /**
     * 正在输入状态Key前缀
     * 格式：chat:typing:{conversationId}:{userId}
     */
    public static final String REDIS_KEY_TYPING = "chat:typing:";

    /**
     * 会话成员列表Key前缀
     * 格式：chat:members:{conversationId}
     */
    public static final String REDIS_KEY_CONVERSATION_MEMBERS = "chat:members:";

    /**
     * 消息设置Key前缀
     * 格式：chat:settings:{userId}
     */
    public static final String REDIS_KEY_MESSAGE_SETTINGS = "chat:settings:";

    /**
     * 未读数量Key前缀
     * 格式：chat:unread:{userId}
     */
    public static final String REDIS_KEY_UNREAD_COUNT = "chat:unread:";

    // ========== 消息限制 ==========
    
    /**
     * 文本消息最大长度（字符）
     */
    public static final int MESSAGE_TEXT_MAX_LENGTH = 5000;

    /**
     * 系统消息最大长度（字符）
     */
    public static final int MESSAGE_SYSTEM_MAX_LENGTH = 1000;

    /**
     * 图片最大大小（字节，10MB）
     */
    public static final long MESSAGE_IMAGE_MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 语音最大大小（字节，5MB）
     */
    public static final long MESSAGE_VOICE_MAX_SIZE = 5 * 1024 * 1024;

    /**
     * 语音最长时长（秒）
     */
    public static final int MESSAGE_VOICE_MAX_DURATION = 60;

    /**
     * 视频最大大小（字节，50MB）
     */
    public static final long MESSAGE_VIDEO_MAX_SIZE = 50 * 1024 * 1024;

    /**
     * 视频最长时长（秒，5分钟）
     */
    public static final int MESSAGE_VIDEO_MAX_DURATION = 300;

    /**
     * 文件最大大小（字节，20MB）
     */
    public static final long MESSAGE_FILE_MAX_SIZE = 20 * 1024 * 1024;

    // ========== 会话限制 ==========
    
    /**
     * 会话标题最大长度（字符）
     */
    public static final int CONVERSATION_TITLE_MAX_LENGTH = 100;

    /**
     * 会话描述最大长度（字符）
     */
    public static final int CONVERSATION_DESC_MAX_LENGTH = 500;

    /**
     * 群聊最大成员数
     */
    public static final int GROUP_MAX_MEMBERS = 500;

    /**
     * 群聊最小成员数
     */
    public static final int GROUP_MIN_MEMBERS = 2;

    /**
     * 群昵称最大长度（字符）
     */
    public static final int GROUP_NICKNAME_MAX_LENGTH = 100;

    // ========== 功能限制 ==========
    
    /**
     * 消息撤回时限（分钟）
     */
    public static final int MESSAGE_RECALL_MINUTES = 2;

    /**
     * 消息转发最大会话数
     */
    public static final int MESSAGE_FORWARD_MAX_CONVERSATIONS = 10;

    /**
     * 消息搜索结果最大数量
     */
    public static final int MESSAGE_SEARCH_MAX_RESULTS = 100;

    /**
     * 会话列表每页最大数量
     */
    public static final int CONVERSATION_LIST_MAX_SIZE = 50;

    /**
     * 消息列表每页最大数量
     */
    public static final int MESSAGE_LIST_MAX_SIZE = 100;

    // ========== WebSocket配置 ==========
    
    /**
     * WebSocket心跳间隔（秒）
     */
    public static final int WEBSOCKET_HEARTBEAT_INTERVAL = 30;

    /**
     * WebSocket连接超时（秒）
     */
    public static final int WEBSOCKET_CONNECTION_TIMEOUT = 60;

    /**
     * 正在输入状态过期时间（秒）
     */
    public static final int TYPING_STATUS_EXPIRE_SECONDS = 10;

    // ========== 缓存TTL ==========
    
    /**
     * 消息设置缓存TTL（秒，1小时）
     */
    public static final int CACHE_MESSAGE_SETTINGS_TTL = 3600;

    /**
     * 会话成员缓存TTL（秒，5分钟）
     */
    public static final int CACHE_CONVERSATION_MEMBERS_TTL = 300;

    /**
     * 用户在线状态TTL（秒，5分钟）
     */
    public static final int CACHE_USER_ONLINE_TTL = 300;

    // ========== 投递状态 ==========
    
    /**
     * 投递状态：发送中
     */
    public static final int DELIVERY_STATUS_SENDING = 0;

    /**
     * 投递状态：已发送
     */
    public static final int DELIVERY_STATUS_SENT = 1;

    /**
     * 投递状态：已送达
     */
    public static final int DELIVERY_STATUS_DELIVERED = 2;

    /**
     * 投递状态：已读
     */
    public static final int DELIVERY_STATUS_READ = 3;

    /**
     * 投递状态：发送失败
     */
    public static final int DELIVERY_STATUS_FAILED = 4;

    // ========== 消息类型 ==========
    
    /**
     * 消息类型：文本
     */
    public static final int MESSAGE_TYPE_TEXT = 1;

    /**
     * 消息类型：图片
     */
    public static final int MESSAGE_TYPE_IMAGE = 2;

    /**
     * 消息类型：语音
     */
    public static final int MESSAGE_TYPE_VOICE = 3;

    /**
     * 消息类型：视频
     */
    public static final int MESSAGE_TYPE_VIDEO = 4;

    /**
     * 消息类型：文件
     */
    public static final int MESSAGE_TYPE_FILE = 5;

    /**
     * 消息类型：系统通知
     */
    public static final int MESSAGE_TYPE_SYSTEM = 6;

    /**
     * 消息类型：表情
     */
    public static final int MESSAGE_TYPE_EMOJI = 7;

    /**
     * 消息类型：位置
     */
    public static final int MESSAGE_TYPE_LOCATION = 8;

    /**
     * 消息类型：订单卡片
     */
    public static final int MESSAGE_TYPE_ORDER_CARD = 9;

    // ========== 会话类型 ==========
    
    /**
     * 会话类型：私聊
     */
    public static final int CONVERSATION_TYPE_PRIVATE = 1;

    /**
     * 会话类型：群聊
     */
    public static final int CONVERSATION_TYPE_GROUP = 2;

    /**
     * 会话类型：系统通知
     */
    public static final int CONVERSATION_TYPE_SYSTEM = 3;

    /**
     * 会话类型：订单会话
     */
    public static final int CONVERSATION_TYPE_ORDER = 4;
}

