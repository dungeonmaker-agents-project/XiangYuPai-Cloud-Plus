package org.dromara.common.notification.dubbo;

import org.dromara.common.core.domain.R;
import org.dromara.common.notification.domain.Notification;
import org.dromara.common.notification.domain.bo.NotificationCreateBo;
import org.dromara.common.notification.domain.vo.UnreadCountVo;
import org.dromara.common.notification.mapper.NotificationMapper;
import org.dromara.common.notification.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RemoteNotificationService Dubbo实现类单元测试
 * Remote Notification Service Implementation Unit Tests
 *
 * @author XiangYuPai Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("通知服务RPC实现测试")
class RemoteNotificationServiceImplTest {

    @Mock
    private INotificationService notificationService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private RemoteNotificationServiceImpl remoteNotificationService;

    private Long userId;
    private Long fromUserId;
    private Long contentId;
    private Long notificationId;

    @BeforeEach
    void setUp() {
        userId = 2001L;
        fromUserId = 3001L;
        contentId = 5001L;
        notificationId = 1001L;
    }

    @Test
    @DisplayName("发送点赞通知 - 正常情况")
    void testSendLikeNotification_Success() {
        // Given
        when(notificationService.createNotification(any(NotificationCreateBo.class)))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.sendLikeNotification(
            userId, fromUserId, "post", contentId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService).createNotification(argThat(bo ->
            bo.getUserId().equals(userId) &&
            bo.getFromUserId().equals(fromUserId) &&
            bo.getType().equals("like") &&
            bo.getContentType().equals("post") &&
            bo.getContentId().equals(contentId)
        ));
    }

    @Test
    @DisplayName("发送点赞通知 - 给自己发通知")
    void testSendLikeNotification_SelfNotification() {
        // When (userId == fromUserId)
        R<Boolean> result = remoteNotificationService.sendLikeNotification(
            userId, userId, "post", contentId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();

        // Verify - 不应该创建通知
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    @DisplayName("发送评论通知 - 正常情况")
    void testSendCommentNotification_Success() {
        // Given
        String commentText = "这是一条评论";
        when(notificationService.createNotification(any(NotificationCreateBo.class)))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.sendCommentNotification(
            userId, fromUserId, "post", contentId, commentText
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService).createNotification(argThat(bo ->
            bo.getUserId().equals(userId) &&
            bo.getFromUserId().equals(fromUserId) &&
            bo.getType().equals("comment") &&
            bo.getContent().contains(commentText)
        ));
    }

    @Test
    @DisplayName("发送评论通知 - 评论内容超长")
    void testSendCommentNotification_LongComment() {
        // Given
        String longComment = "这是一条很长的评论".repeat(10); // 超过50字
        when(notificationService.createNotification(any(NotificationCreateBo.class)))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.sendCommentNotification(
            userId, fromUserId, "post", contentId, longComment
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();

        // Verify - 内容应该被截断
        verify(notificationService).createNotification(argThat(bo ->
            bo.getContent().contains("...") && bo.getContent().length() <= 100
        ));
    }

    @Test
    @DisplayName("发送评论通知 - 给自己发通知")
    void testSendCommentNotification_SelfNotification() {
        // When
        R<Boolean> result = remoteNotificationService.sendCommentNotification(
            userId, userId, "post", contentId, "评论内容"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    @DisplayName("发送关注通知 - 正常情况")
    void testSendFollowNotification_Success() {
        // Given
        when(notificationService.createNotification(any(NotificationCreateBo.class)))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.sendFollowNotification(
            userId, fromUserId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService).createNotification(argThat(bo ->
            bo.getUserId().equals(userId) &&
            bo.getFromUserId().equals(fromUserId) &&
            bo.getType().equals("follow")
        ));
    }

    @Test
    @DisplayName("发送关注通知 - 给自己发通知")
    void testSendFollowNotification_SelfNotification() {
        // When
        R<Boolean> result = remoteNotificationService.sendFollowNotification(
            userId, userId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    @DisplayName("发送系统通知 - 正常情况")
    void testSendSystemNotification_Success() {
        // Given
        String title = "系统维护通知";
        String content = "系统将于今晚22:00进行维护";

        when(notificationService.createNotification(any(NotificationCreateBo.class)))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.sendSystemNotification(
            userId, title, content
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService).createNotification(argThat(bo ->
            bo.getUserId().equals(userId) &&
            bo.getType().equals("system") &&
            bo.getTitle().equals(title) &&
            bo.getContent().equals(content)
        ));
    }

    @Test
    @DisplayName("批量发送系统通知 - 正常情况")
    void testBatchSendSystemNotification_Success() {
        // Given
        Long[] userIds = {2001L, 2002L, 2003L};
        String title = "新功能上线";
        String content = "我们上线了新功能";

        when(notificationService.batchCreateNotifications(anyList()))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.batchSendSystemNotification(
            userIds, title, content
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService).batchCreateNotifications(argThat(list ->
            list.size() == 3 &&
            list.stream().allMatch(bo -> bo.getType().equals("system"))
        ));
    }

    @Test
    @DisplayName("批量发送系统通知 - 用户列表为空")
    void testBatchSendSystemNotification_EmptyUserList() {
        // When
        R<Boolean> result = remoteNotificationService.batchSendSystemNotification(
            new Long[]{}, "标题", "内容"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService, never()).batchCreateNotifications(any());
    }

    @Test
    @DisplayName("发送活动通知 - 正常情况")
    void testSendActivityNotification_Success() {
        // Given
        Long activityId = 6001L;
        String title = "活动即将开始";
        String content = "您报名的活动将于今晚8点开始";

        when(notificationService.createNotification(any(NotificationCreateBo.class)))
            .thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.sendActivityNotification(
            userId, activityId, title, content
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationService).createNotification(argThat(bo ->
            bo.getUserId().equals(userId) &&
            bo.getType().equals("activity") &&
            bo.getContentType().equals("activity") &&
            bo.getContentId().equals(activityId)
        ));
    }

    @Test
    @DisplayName("获取未读通知总数 - 正常情况")
    void testGetUnreadCount_Success() {
        // Given
        UnreadCountVo countVo = new UnreadCountVo();
        countVo.setLikeCount(5L);
        countVo.setCommentCount(3L);
        countVo.setFollowCount(2L);
        countVo.setSystemCount(1L);
        countVo.setActivityCount(0L);
        countVo.setTotalCount(11L);

        when(notificationService.getUnreadCount(userId)).thenReturn(countVo);

        // When
        R<Long> result = remoteNotificationService.getUnreadCount(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(11L);

        // Verify
        verify(notificationService).getUnreadCount(userId);
    }

    @Test
    @DisplayName("获取指定类型未读数 - like类型")
    void testGetUnreadCountByType_Like() {
        // Given
        UnreadCountVo countVo = new UnreadCountVo();
        countVo.setLikeCount(5L);
        countVo.setCommentCount(3L);
        countVo.setFollowCount(2L);

        when(notificationService.getUnreadCount(userId)).thenReturn(countVo);

        // When
        R<Long> result = remoteNotificationService.getUnreadCountByType(userId, "like");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(5L);

        // Verify
        verify(notificationService).getUnreadCount(userId);
    }

    @Test
    @DisplayName("获取指定类型未读数 - 未知类型")
    void testGetUnreadCountByType_UnknownType() {
        // Given
        UnreadCountVo countVo = new UnreadCountVo();
        countVo.setTotalCount(10L);

        when(notificationService.getUnreadCount(userId)).thenReturn(countVo);

        // When
        R<Long> result = remoteNotificationService.getUnreadCountByType(userId, "unknown");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(0L);
    }

    @Test
    @DisplayName("删除通知 - 正常情况")
    void testDeleteNotification_Success() {
        // Given
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUserId(userId);

        when(notificationMapper.selectById(notificationId)).thenReturn(notification);
        when(notificationService.deleteNotification(notificationId)).thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteNotificationService.deleteNotification(
            notificationId, userId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(notificationMapper).selectById(notificationId);
        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    @DisplayName("删除通知 - 通知不存在")
    void testDeleteNotification_NotFound() {
        // Given
        when(notificationMapper.selectById(notificationId)).thenReturn(null);

        // When
        R<Boolean> result = remoteNotificationService.deleteNotification(
            notificationId, userId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("通知不存在");

        // Verify
        verify(notificationMapper).selectById(notificationId);
        verify(notificationService, never()).deleteNotification(any());
    }

    @Test
    @DisplayName("删除通知 - 无权限")
    void testDeleteNotification_NoPermission() {
        // Given
        Long otherUserId = 4001L;
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUserId(userId);

        when(notificationMapper.selectById(notificationId)).thenReturn(notification);

        // When
        R<Boolean> result = remoteNotificationService.deleteNotification(
            notificationId, otherUserId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("无权限删除此通知");

        // Verify
        verify(notificationMapper).selectById(notificationId);
        verify(notificationService, never()).deleteNotification(any());
    }
}
