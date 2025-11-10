package com.xypai.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户关注事件
 * 
 * 触发时机：用户A关注用户B时发布此事件
 * 监听器：自动更新统计数据（粉丝数+1、关注数+1）
 *
 * @author Bob
 * @date 2025-01-14
 */
@Getter
public class UserFollowEvent extends ApplicationEvent {

    /**
     * 关注者ID（用户A）
     */
    private final Long followerId;

    /**
     * 被关注者ID（用户B）
     */
    private final Long followedUserId;

    /**
     * 是否为关注操作（true=关注，false=取消关注）
     */
    private final boolean isFollow;

    /**
     * 关系类型（1=关注，4=特别关注）
     */
    private final Integer relationType;

    public UserFollowEvent(Object source, Long followerId, Long followedUserId, 
                          boolean isFollow, Integer relationType) {
        super(source);
        this.followerId = followerId;
        this.followedUserId = followedUserId;
        this.isFollow = isFollow;
        this.relationType = relationType;
    }

    /**
     * 创建关注事件
     */
    public static UserFollowEvent follow(Object source, Long followerId, Long followedUserId) {
        return new UserFollowEvent(source, followerId, followedUserId, true, 1);
    }

    /**
     * 创建取消关注事件
     */
    public static UserFollowEvent unfollow(Object source, Long followerId, Long followedUserId) {
        return new UserFollowEvent(source, followerId, followedUserId, false, 1);
    }
}

