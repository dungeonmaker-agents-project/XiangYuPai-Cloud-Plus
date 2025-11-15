package org.dromara.user.service;

import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.BaseTest;
import org.dromara.user.domain.dto.UserBlockDto;
import org.dromara.user.domain.dto.UserReportDto;
import org.dromara.user.domain.entity.UserBlacklist;
import org.dromara.user.domain.entity.UserRelation;
import org.dromara.user.domain.vo.UserRelationVo;
import org.dromara.user.mapper.UserBlacklistMapper;
import org.dromara.user.mapper.UserRelationMapper;
import org.dromara.user.mapper.UserReportMapper;
import org.dromara.user.service.impl.RelationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RelationService 测试类
 *
 * 测试业务逻辑层:
 * - 关注/取消关注
 * - 粉丝列表查询
 * - 关注列表查询
 * - 拉黑/取消拉黑
 * - 举报用户
 * - 业务规则验证
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("用户关系服务测试 - RelationService Tests")
public class RelationServiceTest extends BaseTest {

    @Mock
    private UserRelationMapper relationMapper;

    @Mock
    private UserBlacklistMapper blacklistMapper;

    @Mock
    private UserReportMapper reportMapper;

    @InjectMocks
    private RelationServiceImpl relationService;

    // ==================== 关注测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-001: 关注用户 - 成功")
    public void testFollowUser_Success() {
        // Given
        Long followingId = TEST_OTHER_USER_ID;

        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, followingId))
            .thenReturn(null); // 未关注过
        when(blacklistMapper.selectByUserAndBlocked(TEST_USER_ID, followingId))
            .thenReturn(null); // 未拉黑
        when(relationMapper.insert(any(UserRelation.class))).thenReturn(1);

        // When
        R<Void> result = relationService.followUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(relationMapper, times(1)).insert(any(UserRelation.class));
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-002: 关注用户 - 不能关注自己")
    public void testFollowUser_CannotFollowSelf() {
        // Given: 尝试关注自己
        Long followingId = TEST_USER_ID;

        // When
        R<Void> result = relationService.followUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("自己"));
        verify(relationMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-003: 关注用户 - 已经关注过")
    public void testFollowUser_AlreadyFollowing() {
        // Given: 已经关注过
        Long followingId = TEST_OTHER_USER_ID;

        UserRelation existingRelation = new UserRelation();
        existingRelation.setFollowerId(TEST_USER_ID);
        existingRelation.setFollowingId(followingId);

        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, followingId))
            .thenReturn(existingRelation);

        // When
        R<Void> result = relationService.followUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("已经关注") || result.getMsg().contains("重复"));
        verify(relationMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-004: 关注用户 - 对方已拉黑我")
    public void testFollowUser_BlockedByTarget() {
        // Given: 对方已拉黑我
        Long followingId = TEST_OTHER_USER_ID;

        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, followingId))
            .thenReturn(null);

        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setUserId(followingId);
        blacklist.setBlockedUserId(TEST_USER_ID);

        when(blacklistMapper.selectByUserAndBlocked(followingId, TEST_USER_ID))
            .thenReturn(blacklist);

        // When
        R<Void> result = relationService.followUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("无法关注") || result.getMsg().contains("拉黑"));
        verify(relationMapper, never()).insert(any());
    }

    // ==================== 取消关注测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-005: 取消关注 - 成功")
    public void testUnfollowUser_Success() {
        // Given
        Long followingId = TEST_OTHER_USER_ID;

        UserRelation existingRelation = new UserRelation();
        existingRelation.setRelationId(1L);
        existingRelation.setFollowerId(TEST_USER_ID);
        existingRelation.setFollowingId(followingId);

        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, followingId))
            .thenReturn(existingRelation);
        when(relationMapper.deleteById(1L)).thenReturn(1);

        // When
        R<Void> result = relationService.unfollowUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(relationMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-006: 取消关注 - 未关注过该用户")
    public void testUnfollowUser_NotFollowing() {
        // Given
        Long followingId = TEST_OTHER_USER_ID;

        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, followingId))
            .thenReturn(null);

        // When
        R<Void> result = relationService.unfollowUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("未关注"));
        verify(relationMapper, never()).deleteById(any());
    }

    // ==================== 粉丝列表测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-007: 获取粉丝列表 - 成功")
    public void testGetFansList_Success() {
        // Given
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        when(relationMapper.selectFansList(eq(TEST_USER_ID), isNull(), any()))
            .thenReturn(Arrays.asList());

        // When
        TableDataInfo<UserRelationVo> result = relationService.getFansList(
            TEST_USER_ID, null, pageQuery
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getRows());
        verify(relationMapper, times(1)).selectFansList(eq(TEST_USER_ID), isNull(), any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-008: 搜索粉丝 - 按昵称")
    public void testGetFansList_WithKeyword() {
        // Given
        String keyword = "张三";
        PageQuery pageQuery = new PageQuery();

        when(relationMapper.selectFansList(eq(TEST_USER_ID), eq(keyword), any()))
            .thenReturn(Arrays.asList());

        // When
        TableDataInfo<UserRelationVo> result = relationService.getFansList(
            TEST_USER_ID, keyword, pageQuery
        );

        // Then
        assertNotNull(result);
        verify(relationMapper, times(1)).selectFansList(eq(TEST_USER_ID), eq(keyword), any());
    }

    // ==================== 关注列表测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-009: 获取关注列表 - 成功")
    public void testGetFollowingList_Success() {
        // Given
        PageQuery pageQuery = new PageQuery();

        when(relationMapper.selectFollowingList(eq(TEST_USER_ID), isNull(), any()))
            .thenReturn(Arrays.asList());

        // When
        TableDataInfo<UserRelationVo> result = relationService.getFollowingList(
            TEST_USER_ID, null, pageQuery
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getRows());
        verify(relationMapper, times(1)).selectFollowingList(eq(TEST_USER_ID), isNull(), any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-010: 搜索关注 - 按昵称")
    public void testGetFollowingList_WithKeyword() {
        // Given
        String keyword = "李四";
        PageQuery pageQuery = new PageQuery();

        when(relationMapper.selectFollowingList(eq(TEST_USER_ID), eq(keyword), any()))
            .thenReturn(Arrays.asList());

        // When
        TableDataInfo<UserRelationVo> result = relationService.getFollowingList(
            TEST_USER_ID, keyword, pageQuery
        );

        // Then
        assertNotNull(result);
        verify(relationMapper, times(1)).selectFollowingList(eq(TEST_USER_ID), eq(keyword), any());
    }

    // ==================== 拉黑测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-011: 拉黑用户 - 成功")
    public void testBlockUser_Success() {
        // Given
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_OTHER_USER_ID);
        dto.setReason("骚扰他人");

        when(blacklistMapper.selectByUserAndBlocked(TEST_USER_ID, TEST_OTHER_USER_ID))
            .thenReturn(null); // 未拉黑过
        when(blacklistMapper.insert(any(UserBlacklist.class))).thenReturn(1);

        // When
        R<Void> result = relationService.blockUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(blacklistMapper, times(1)).insert(any(UserBlacklist.class));
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-012: 拉黑用户 - 不能拉黑自己")
    public void testBlockUser_CannotBlockSelf() {
        // Given
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_USER_ID); // 拉黑自己
        dto.setReason("测试");

        // When
        R<Void> result = relationService.blockUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("自己"));
        verify(blacklistMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-013: 拉黑用户 - 已经拉黑过")
    public void testBlockUser_AlreadyBlocked() {
        // Given
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_OTHER_USER_ID);
        dto.setReason("测试");

        UserBlacklist existingBlock = new UserBlacklist();
        existingBlock.setUserId(TEST_USER_ID);
        existingBlock.setBlockedUserId(TEST_OTHER_USER_ID);

        when(blacklistMapper.selectByUserAndBlocked(TEST_USER_ID, TEST_OTHER_USER_ID))
            .thenReturn(existingBlock);

        // When
        R<Void> result = relationService.blockUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("已经拉黑"));
        verify(blacklistMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-014: 拉黑后自动取消关注")
    public void testBlockUser_AutoUnfollow() {
        // Given
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_OTHER_USER_ID);
        dto.setReason("测试");

        // 已关注该用户
        UserRelation existingRelation = new UserRelation();
        existingRelation.setRelationId(1L);

        when(blacklistMapper.selectByUserAndBlocked(TEST_USER_ID, TEST_OTHER_USER_ID))
            .thenReturn(null);
        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, TEST_OTHER_USER_ID))
            .thenReturn(existingRelation);
        when(blacklistMapper.insert(any(UserBlacklist.class))).thenReturn(1);
        when(relationMapper.deleteById(1L)).thenReturn(1);

        // When
        R<Void> result = relationService.blockUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(blacklistMapper, times(1)).insert(any());
        verify(relationMapper, times(1)).deleteById(1L); // 自动取消关注
    }

    // ==================== 取消拉黑测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-015: 取消拉黑 - 成功")
    public void testUnblockUser_Success() {
        // Given
        Long blockedUserId = TEST_OTHER_USER_ID;

        UserBlacklist existingBlock = new UserBlacklist();
        existingBlock.setBlacklistId(1L);
        existingBlock.setUserId(TEST_USER_ID);
        existingBlock.setBlockedUserId(blockedUserId);

        when(blacklistMapper.selectByUserAndBlocked(TEST_USER_ID, blockedUserId))
            .thenReturn(existingBlock);
        when(blacklistMapper.deleteById(1L)).thenReturn(1);

        // When
        R<Void> result = relationService.unblockUser(TEST_USER_ID, blockedUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(blacklistMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-016: 取消拉黑 - 未拉黑过该用户")
    public void testUnblockUser_NotBlocked() {
        // Given
        Long blockedUserId = TEST_OTHER_USER_ID;

        when(blacklistMapper.selectByUserAndBlocked(TEST_USER_ID, blockedUserId))
            .thenReturn(null);

        // When
        R<Void> result = relationService.unblockUser(TEST_USER_ID, blockedUserId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("未拉黑"));
        verify(blacklistMapper, never()).deleteById(any());
    }

    // ==================== 举报测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-017: 举报用户 - 成功")
    public void testReportUser_Success() {
        // Given
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_OTHER_USER_ID);
        dto.setReportType(1); // 违规内容
        dto.setReportReason("发布违规内容");
        dto.setReportImages(Arrays.asList("https://cdn.example.com/evidence.jpg"));

        when(reportMapper.insert(any())).thenReturn(1);

        // When
        R<Void> result = relationService.reportUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(reportMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-018: 举报用户 - 不能举报自己")
    public void testReportUser_CannotReportSelf() {
        // Given
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_USER_ID); // 举报自己
        dto.setReportType(1);
        dto.setReportReason("测试");

        // When
        R<Void> result = relationService.reportUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("自己"));
        verify(reportMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-019: 举报用户 - 举报原因必填")
    public void testReportUser_ReasonRequired() {
        // Given
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_OTHER_USER_ID);
        dto.setReportType(1);
        dto.setReportReason(""); // 空原因

        // When
        R<Void> result = relationService.reportUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("原因") || result.getMsg().contains("必填"));
        verify(reportMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-020: 举报用户 - 多种举报类型")
    public void testReportUser_DifferentTypes() {
        // Given: 测试4种举报类型
        int[] types = {1, 2, 3, 4}; // 违规内容、骚扰、欺诈、其他

        for (int type : types) {
            UserReportDto dto = new UserReportDto();
            dto.setReportedUserId(TEST_OTHER_USER_ID);
            dto.setReportType(type);
            dto.setReportReason("举报原因" + type);

            when(reportMapper.insert(any())).thenReturn(1);

            R<Void> result = relationService.reportUser(TEST_USER_ID, dto);

            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        verify(reportMapper, times(4)).insert(any());
    }

    // ==================== 业务规则测试 ====================

    @Test
    @DisplayName("TC-RELATION-SERVICE-021: 互相关注状态判断")
    public void testMutualFollowStatus() {
        // Given: 查询粉丝列表时,需要判断是否互相关注
        PageQuery pageQuery = new PageQuery();

        UserRelationVo fan1 = UserRelationVo.builder()
            .userId(2001L)
            .isFollowing(false) // 未回关
            .isMutualFollow(false)
            .build();

        UserRelationVo fan2 = UserRelationVo.builder()
            .userId(2002L)
            .isFollowing(true) // 已回关
            .isMutualFollow(true)
            .build();

        when(relationMapper.selectFansList(eq(TEST_USER_ID), isNull(), any()))
            .thenReturn(Arrays.asList(fan1, fan2));

        // When
        TableDataInfo<UserRelationVo> result = relationService.getFansList(
            TEST_USER_ID, null, pageQuery
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRows().size());

        // 验证互关状态
        UserRelationVo resultFan1 = result.getRows().get(0);
        assertFalse(resultFan1.getIsMutualFollow());

        UserRelationVo resultFan2 = result.getRows().get(1);
        assertTrue(resultFan2.getIsMutualFollow());
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-022: 拉黑原因长度限制")
    public void testBlockReason_LengthLimit() {
        // Given: 拉黑原因超过200字符
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_OTHER_USER_ID);

        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            longReason.append("A");
        }
        dto.setReason(longReason.toString());

        // When
        R<Void> result = relationService.blockUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("原因") || result.getMsg().contains("长度"));
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-023: 举报证据图片数量限制")
    public void testReportImages_CountLimit() {
        // Given: 举报图片超过5张
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_OTHER_USER_ID);
        dto.setReportType(1);
        dto.setReportReason("违规");
        dto.setReportImages(Arrays.asList(
            "url1", "url2", "url3", "url4", "url5", "url6" // 6张,超过限制
        ));

        // When
        R<Void> result = relationService.reportUser(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("图片") || result.getMsg().contains("5"));
    }

    @Test
    @DisplayName("TC-RELATION-SERVICE-024: 关注数量限制")
    public void testFollowCount_Limit() {
        // Given: 用户已关注1000个人
        Long followingId = TEST_OTHER_USER_ID;

        when(relationMapper.selectByFollowerAndFollowing(TEST_USER_ID, followingId))
            .thenReturn(null);
        when(relationMapper.countFollowingByUserId(TEST_USER_ID))
            .thenReturn(1000L); // 已关注1000人

        // When
        R<Void> result = relationService.followUser(TEST_USER_ID, followingId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("数量") || result.getMsg().contains("限制"));
        verify(relationMapper, never()).insert(any());
    }
}
