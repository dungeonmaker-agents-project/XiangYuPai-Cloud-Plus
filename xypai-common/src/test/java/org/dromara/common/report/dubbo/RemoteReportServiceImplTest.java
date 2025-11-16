package org.dromara.common.report.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.core.domain.R;
import org.dromara.common.report.domain.entity.Punishment;
import org.dromara.common.report.domain.entity.Report;
import org.dromara.common.report.mapper.PunishmentMapper;
import org.dromara.common.report.mapper.ReportMapper;
import org.dromara.common.report.service.IReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * RemoteReportService Dubbo实现类单元测试
 * Remote Report Service Implementation Unit Tests
 *
 * @author XiangYuPai Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("举报服务RPC实现测试")
public class RemoteReportServiceImplTest {

    @Mock
    private IReportService reportService;

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private PunishmentMapper punishmentMapper;

    @InjectMocks
    private RemoteReportServiceImpl remoteReportService;

    private Long userId;
    private Long reporterId;
    private Long contentId;

    @BeforeEach
    void setUp() {
        userId = 2001L;
        reporterId = 3001L;
        contentId = 5001L;
    }

    @Test
    @DisplayName("检查用户是否被封禁 - 已封禁")
    void testIsUserBanned_True() {
        // Given
        when(reportService.isUserBanned(userId)).thenReturn(true);

        // When
        R<Boolean> result = remoteReportService.isUserBanned(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(reportService).isUserBanned(userId);
    }

    @Test
    @DisplayName("检查用户是否被封禁 - 未封禁")
    void testIsUserBanned_False() {
        // Given
        when(reportService.isUserBanned(userId)).thenReturn(false);

        // When
        R<Boolean> result = remoteReportService.isUserBanned(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(reportService).isUserBanned(userId);
    }

    @Test
    @DisplayName("检查用户是否被禁言 - 已禁言")
    void testIsUserMuted_True() {
        // Given
        when(reportService.isUserMuted(userId)).thenReturn(true);

        // When
        R<Boolean> result = remoteReportService.isUserMuted(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(reportService).isUserMuted(userId);
    }

    @Test
    @DisplayName("检查用户是否可以发布 - 可以发布")
    void testCanUserPost_True() {
        // Given
        when(reportService.isUserBanned(userId)).thenReturn(false);
        when(reportService.isUserMuted(userId)).thenReturn(false);

        // When
        R<Boolean> result = remoteReportService.canUserPost(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(reportService).isUserBanned(userId);
        verify(reportService).isUserMuted(userId);
    }

    @Test
    @DisplayName("检查用户是否可以发布 - 被封禁不能发布")
    void testCanUserPost_Banned() {
        // Given
        when(reportService.isUserBanned(userId)).thenReturn(true);
        when(reportService.isUserMuted(userId)).thenReturn(false);

        // When
        R<Boolean> result = remoteReportService.canUserPost(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();
    }

    @Test
    @DisplayName("检查用户是否可以发布 - 被禁言不能发布")
    void testCanUserPost_Muted() {
        // Given
        when(reportService.isUserBanned(userId)).thenReturn(false);
        when(reportService.isUserMuted(userId)).thenReturn(true);

        // When
        R<Boolean> result = remoteReportService.canUserPost(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();
    }

    @Test
    @DisplayName("检查内容是否被举报 - 已被举报")
    void testIsContentReported_True() {
        // Given
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // When
        R<Boolean> result = remoteReportService.isContentReported("post", contentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(reportMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("检查内容是否被举报 - 未被举报")
    void testIsContentReported_False() {
        // Given
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        R<Boolean> result = remoteReportService.isContentReported("post", contentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(reportMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取内容被举报次数 - 正常情况")
    void testGetReportCount_Success() {
        // Given
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When
        R<Integer> result = remoteReportService.getReportCount("post", contentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(5);

        // Verify
        verify(reportMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("封禁用户 - 临时封禁")
    void testBanUser_Temporary() {
        // Given
        Integer duration = 1440; // 24小时
        String reason = "违规发布内容";

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(punishmentMapper.insert(any(Punishment.class))).thenReturn(1);

        // When
        R<Boolean> result = remoteReportService.banUser(userId, duration, reason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(punishmentMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(punishmentMapper).insert(argThat(punishment ->
            punishment.getUserId().equals(userId) &&
            punishment.getType().equals("ban") &&
            punishment.getDuration().equals(duration) &&
            punishment.getReason().equals(reason) &&
            punishment.getEndTime() != null  // 临时封禁有结束时间
        ));
    }

    @Test
    @DisplayName("封禁用户 - 永久封禁")
    void testBanUser_Permanent() {
        // Given
        String reason = "严重违规";

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(punishmentMapper.insert(any(Punishment.class))).thenReturn(1);

        // When
        R<Boolean> result = remoteReportService.banUser(userId, null, reason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(punishmentMapper).insert(argThat(punishment ->
            punishment.getUserId().equals(userId) &&
            punishment.getType().equals("ban") &&
            punishment.getDuration() == null &&  // 永久封禁无时长
            punishment.getEndTime() == null      // 永久封禁无结束时间
        ));
    }

    @Test
    @DisplayName("封禁用户 - 用户已被封禁")
    void testBanUser_AlreadyBanned() {
        // Given
        Punishment existingBan = new Punishment();
        existingBan.setUserId(userId);
        existingBan.setType("ban");
        existingBan.setStatus(0);

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class)))
            .thenReturn(existingBan);

        // When
        R<Boolean> result = remoteReportService.banUser(userId, 1440, "违规");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("用户已被封禁");

        // Verify
        verify(punishmentMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(punishmentMapper, never()).insert(any());
    }

    @Test
    @DisplayName("禁言用户 - 正常情况")
    void testMuteUser_Success() {
        // Given
        Integer duration = 720; // 12小时
        String reason = "恶意评论";

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(punishmentMapper.insert(any(Punishment.class))).thenReturn(1);

        // When
        R<Boolean> result = remoteReportService.muteUser(userId, duration, reason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(punishmentMapper).insert(argThat(punishment ->
            punishment.getUserId().equals(userId) &&
            punishment.getType().equals("mute") &&
            punishment.getDuration().equals(duration)
        ));
    }

    @Test
    @DisplayName("禁言用户 - 用户已被禁言")
    void testMuteUser_AlreadyMuted() {
        // Given
        Punishment existingMute = new Punishment();
        existingMute.setUserId(userId);
        existingMute.setType("mute");
        existingMute.setStatus(0);

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class)))
            .thenReturn(existingMute);

        // When
        R<Boolean> result = remoteReportService.muteUser(userId, 720, "恶意评论");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("用户已被禁言");

        // Verify
        verify(punishmentMapper, never()).insert(any());
    }

    @Test
    @DisplayName("解除封禁 - 正常情况")
    void testUnbanUser_Success() {
        // Given
        Punishment punishment = new Punishment();
        punishment.setId(1001L);
        punishment.setUserId(userId);
        punishment.setType("ban");
        punishment.setStatus(0);

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(punishment);
        when(punishmentMapper.updateById(any(Punishment.class))).thenReturn(1);

        // When
        R<Boolean> result = remoteReportService.unbanUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(punishmentMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(punishmentMapper).updateById(argThat(p -> p.getStatus().equals(1)));
    }

    @Test
    @DisplayName("解除封禁 - 用户未被封禁")
    void testUnbanUser_NotBanned() {
        // Given
        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        R<Boolean> result = remoteReportService.unbanUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("用户未被封禁");

        // Verify
        verify(punishmentMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(punishmentMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("解除禁言 - 正常情况")
    void testUnmuteUser_Success() {
        // Given
        Punishment punishment = new Punishment();
        punishment.setId(1002L);
        punishment.setUserId(userId);
        punishment.setType("mute");
        punishment.setStatus(0);

        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(punishment);
        when(punishmentMapper.updateById(any(Punishment.class))).thenReturn(1);

        // When
        R<Boolean> result = remoteReportService.unmuteUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(punishmentMapper).updateById(argThat(p -> p.getStatus().equals(1)));
    }

    @Test
    @DisplayName("解除禁言 - 用户未被禁言")
    void testUnmuteUser_NotMuted() {
        // Given
        when(punishmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        R<Boolean> result = remoteReportService.unmuteUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("用户未被禁言");

        // Verify
        verify(punishmentMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("获取用户被举报次数 - 正常情况")
    void testGetUserReportCount_Success() {
        // Given
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(8L);

        // When
        R<Integer> result = remoteReportService.getUserReportCount(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(8);

        // Verify
        verify(reportMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("检查重复举报 - 已举报过")
    void testIsDuplicateReport_True() {
        // Given
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When
        R<Boolean> result = remoteReportService.isDuplicateReport(
            reporterId, "post", contentId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(reportMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("检查重复举报 - 未举报过")
    void testIsDuplicateReport_False() {
        // Given
        when(reportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        R<Boolean> result = remoteReportService.isDuplicateReport(
            reporterId, "post", contentId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(reportMapper).selectCount(any(LambdaQueryWrapper.class));
    }
}
