package com.xypai.user.listener;

import com.xypai.user.event.UserFollowEvent;
import com.xypai.user.service.IUserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户统计事件监听器
 * 
 * 功能：
 * 1. 监听用户关注事件，自动更新统计数据
 * 2. 异步处理，不阻塞主业务
 * 3. 事务提交后执行，保证数据一致性
 *
 * @author Bob
 * @date 2025-01-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatsEventListener {

    private final IUserStatsService userStatsService;

    /**
     * 监听用户关注事件
     * 
     * @param event 关注事件
     */
    @Async
    @TransactionalEventListener
    public void handleUserFollowEvent(UserFollowEvent event) {
        log.info("收到用户关注事件，followerId: {}, followedUserId: {}, isFollow: {}", 
                event.getFollowerId(), event.getFollowedUserId(), event.isFollow());

        try {
            if (event.isFollow()) {
                // 关注操作
                handleFollow(event.getFollowerId(), event.getFollowedUserId());
            } else {
                // 取消关注操作
                handleUnfollow(event.getFollowerId(), event.getFollowedUserId());
            }
        } catch (Exception e) {
            log.error("处理用户关注事件失败", e);
        }
    }

    /**
     * 处理关注操作
     */
    private void handleFollow(Long followerId, Long followedUserId) {
        // 1. 增加被关注者的粉丝数
        userStatsService.incrementFollowerCount(followedUserId);
        log.debug("增加粉丝数成功，userId: {}", followedUserId);

        // 2. 增加关注者的关注数
        userStatsService.incrementFollowingCount(followerId);
        log.debug("增加关注数成功，userId: {}", followerId);

        log.info("关注统计更新成功，follower: {}, followed: {}", followerId, followedUserId);
    }

    /**
     * 处理取消关注操作
     */
    private void handleUnfollow(Long followerId, Long followedUserId) {
        // 1. 减少被关注者的粉丝数
        userStatsService.decrementFollowerCount(followedUserId);
        log.debug("减少粉丝数成功，userId: {}", followedUserId);

        // 2. 减少关注者的关注数
        userStatsService.decrementFollowingCount(followerId);
        log.debug("减少关注数成功，userId: {}", followerId);

        log.info("取消关注统计更新成功，follower: {}, followed: {}", followerId, followedUserId);
    }
}

