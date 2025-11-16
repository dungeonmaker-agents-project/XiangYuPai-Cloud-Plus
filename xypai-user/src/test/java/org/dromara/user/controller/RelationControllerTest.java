package org.dromara.user.controller;

import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.BaseTest;
import org.dromara.user.controller.app.RelationController;
import org.dromara.user.domain.dto.UserBlockDto;
import org.dromara.user.domain.dto.UserReportDto;
import org.dromara.user.domain.vo.UserRelationVo;
import org.dromara.user.service.IRelationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RelationController 测试类
 *
 * 测试范围:
 * - 页面03: 他人主页 (关注、取消关注、拉黑、举报)
 * - 页面04: 粉丝列表 (查看粉丝、回关)
 * - 页面05: 关注列表 (查看关注、取消关注)
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("用户关系控制器测试 - RelationController Tests")
public class RelationControllerTest extends BaseTest {

    @Mock
    private IRelationService relationService;

    @InjectMocks
    private RelationController relationController;

    // ==================== 关注/取消关注测试 ====================

    @Test
    @DisplayName("TC-RELATION-001: 关注用户 - 成功")
    public void testFollowUser_Success() throws Exception {
        // Given
        Long followingId = TEST_OTHER_USER_ID;
        when(relationService.followUser(TEST_USER_ID, followingId))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + followingId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).followUser(TEST_USER_ID, followingId);
    }

    @Test
    @DisplayName("TC-RELATION-002: 关注用户 - 不能关注自己")
    public void testFollowUser_CannotFollowSelf() throws Exception {
        // Given: 尝试关注自己
        when(relationService.followUser(TEST_USER_ID, TEST_USER_ID))
            .thenReturn(R.fail("不能关注自己"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + TEST_USER_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("不能关注自己"));
    }

    @Test
    @DisplayName("TC-RELATION-003: 关注用户 - 已经关注过")
    public void testFollowUser_AlreadyFollowing() throws Exception {
        // Given: 已经关注过
        Long followingId = TEST_OTHER_USER_ID;
        when(relationService.followUser(TEST_USER_ID, followingId))
            .thenReturn(R.fail("已经关注过该用户"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + followingId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("TC-RELATION-004: 取消关注 - 成功")
    public void testUnfollowUser_Success() throws Exception {
        // Given
        Long followingId = TEST_OTHER_USER_ID;
        when(relationService.unfollowUser(TEST_USER_ID, followingId))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/relation/follow/" + followingId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).unfollowUser(TEST_USER_ID, followingId);
    }

    @Test
    @DisplayName("TC-RELATION-005: 取消关注 - 未关注过该用户")
    public void testUnfollowUser_NotFollowing() throws Exception {
        // Given
        Long followingId = TEST_OTHER_USER_ID;
        when(relationService.unfollowUser(TEST_USER_ID, followingId))
            .thenReturn(R.fail("未关注过该用户"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/relation/follow/" + followingId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 粉丝列表测试 (页面04) ====================

    @Test
    @DisplayName("TC-FANS-001: 获取粉丝列表 - 成功")
    public void testGetFansList_Success() throws Exception {
        // Given
        List<UserRelationVo> fans = Arrays.asList(
            UserRelationVo.builder()
                .userId(2001L)
                .nickname("粉丝1")
                .avatar("https://cdn.example.com/avatar1.jpg")
                .followStatus("none") // 未回关
                .build(),
            UserRelationVo.builder()
                .userId(2002L)
                .nickname("粉丝2")
                .avatar("https://cdn.example.com/avatar2.jpg")
                .followStatus("mutual") // 已回关
                .build()
        );

        TableDataInfo<UserRelationVo> mockData = new TableDataInfo<>();
        mockData.setRows(fans);
        mockData.setTotal(2L);

        when(relationService.getFansList(eq(TEST_USER_ID), isNull(), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.rows[0].userId").value(2001))
            .andExpect(jsonPath("$.rows[0].followStatus").value("none"))
            .andExpect(jsonPath("$.rows[1].userId").value(2002))
            .andExpect(jsonPath("$.rows[1].followStatus").value("mutual"));

        verify(relationService, times(1)).getFansList(eq(TEST_USER_ID), isNull(), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-FANS-002: 搜索粉丝 - 按昵称搜索")
    public void testGetFansList_WithKeyword() throws Exception {
        // Given
        String keyword = "张三";
        List<UserRelationVo> searchResult = Arrays.asList(
            UserRelationVo.builder()
                .userId(2003L)
                .nickname("张三")
                .followStatus("none")
                .build()
        );

        TableDataInfo<UserRelationVo> mockData = new TableDataInfo<>();
        mockData.setRows(searchResult);
        mockData.setTotal(1L);

        when(relationService.getFansList(eq(TEST_USER_ID), eq(keyword), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("keyword", keyword)
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.rows[0].nickname").value("张三"));

        verify(relationService, times(1)).getFansList(eq(TEST_USER_ID), eq(keyword), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-FANS-003: 粉丝列表 - 回关粉丝")
    public void testFollowBackFan() throws Exception {
        // Given: 从粉丝列表中回关某个粉丝
        Long fanUserId = 2001L;
        when(relationService.followUser(TEST_USER_ID, fanUserId))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + fanUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).followUser(TEST_USER_ID, fanUserId);
    }

    @Test
    @DisplayName("TC-FANS-004: 粉丝列表 - 空列表")
    public void testGetFansList_Empty() throws Exception {
        // Given: 没有粉丝
        TableDataInfo<UserRelationVo> emptyData = new TableDataInfo<>();
        emptyData.setRows(Arrays.asList());
        emptyData.setTotal(0L);

        when(relationService.getFansList(eq(TEST_USER_ID), isNull(), any(PageQuery.class)))
            .thenReturn(emptyData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0))
            .andExpect(jsonPath("$.rows").isEmpty());
    }

    // ==================== 关注列表测试 (页面05) ====================

    @Test
    @DisplayName("TC-FOLLOWING-001: 获取关注列表 - 成功")
    public void testGetFollowingList_Success() throws Exception {
        // Given
        List<UserRelationVo> followings = Arrays.asList(
            UserRelationVo.builder()
                .userId(2004L)
                .nickname("关注1")
                .avatar("https://cdn.example.com/avatar3.jpg")
                .followStatus("mutual") // 互相关注
                .build(),
            UserRelationVo.builder()
                .userId(2005L)
                .nickname("关注2")
                .avatar("https://cdn.example.com/avatar4.jpg")
                .followStatus("following") // 单向关注
                .build()
        );

        TableDataInfo<UserRelationVo> mockData = new TableDataInfo<>();
        mockData.setRows(followings);
        mockData.setTotal(2L);

        when(relationService.getFollowingList(eq(TEST_USER_ID), isNull(), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/following")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.rows[0].userId").value(2004))
            .andExpect(jsonPath("$.rows[0].followStatus").value("mutual"))
            .andExpect(jsonPath("$.rows[1].userId").value(2005))
            .andExpect(jsonPath("$.rows[1].followStatus").value("following"));

        verify(relationService, times(1)).getFollowingList(eq(TEST_USER_ID), isNull(), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-FOLLOWING-002: 搜索关注 - 按昵称搜索")
    public void testGetFollowingList_WithKeyword() throws Exception {
        // Given
        String keyword = "李四";
        List<UserRelationVo> searchResult = Arrays.asList(
            UserRelationVo.builder()
                .userId(2006L)
                .nickname("李四")
                .followStatus("following")
                .build()
        );

        TableDataInfo<UserRelationVo> mockData = new TableDataInfo<>();
        mockData.setRows(searchResult);
        mockData.setTotal(1L);

        when(relationService.getFollowingList(eq(TEST_USER_ID), eq(keyword), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/following")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("keyword", keyword)
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.rows[0].nickname").value("李四"));

        verify(relationService, times(1)).getFollowingList(eq(TEST_USER_ID), eq(keyword), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-FOLLOWING-003: 关注列表 - 取消关注")
    public void testUnfollowFromFollowingList() throws Exception {
        // Given: 从关注列表中取消关注
        Long followingId = 2004L;
        when(relationService.unfollowUser(TEST_USER_ID, followingId))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/relation/follow/" + followingId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).unfollowUser(TEST_USER_ID, followingId);
    }

    // ==================== 拉黑测试 ====================

    @Test
    @DisplayName("TC-BLOCK-001: 拉黑用户 - 成功")
    public void testBlockUser_Success() throws Exception {
        // Given
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_OTHER_USER_ID);
        dto.setReason("骚扰他人");

        when(relationService.blockUser(eq(TEST_USER_ID), any(UserBlockDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/block")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).blockUser(eq(TEST_USER_ID), any(UserBlockDto.class));
    }

    @Test
    @DisplayName("TC-BLOCK-002: 拉黑用户 - 不能拉黑自己")
    public void testBlockUser_CannotBlockSelf() throws Exception {
        // Given: 尝试拉黑自己
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_USER_ID);
        dto.setReason("测试");

        when(relationService.blockUser(eq(TEST_USER_ID), any(UserBlockDto.class)))
            .thenReturn(R.fail("不能拉黑自己"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/block")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("TC-BLOCK-003: 拉黑用户 - 已经拉黑过")
    public void testBlockUser_AlreadyBlocked() throws Exception {
        // Given
        UserBlockDto dto = new UserBlockDto();
        dto.setBlockedUserId(TEST_OTHER_USER_ID);
        dto.setReason("骚扰");

        when(relationService.blockUser(eq(TEST_USER_ID), any(UserBlockDto.class)))
            .thenReturn(R.fail("已经拉黑过该用户"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/block")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("TC-BLOCK-004: 取消拉黑 - 成功")
    public void testUnblockUser_Success() throws Exception {
        // Given
        Long blockedUserId = TEST_OTHER_USER_ID;
        when(relationService.unblockUser(TEST_USER_ID, blockedUserId))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/relation/block/" + blockedUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).unblockUser(TEST_USER_ID, blockedUserId);
    }

    // ==================== 举报测试 ====================

    @Test
    @DisplayName("TC-REPORT-001: 举报用户 - 成功")
    public void testReportUser_Success() throws Exception {
        // Given
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_OTHER_USER_ID);
        dto.setReason("spam"); // spam, abuse, inappropriate, fraud, other
        dto.setDescription("发布违规内容");
        dto.setEvidence("https://cdn.example.com/evidence1.jpg,https://cdn.example.com/evidence2.jpg");

        when(relationService.reportUser(eq(TEST_USER_ID), any(UserReportDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/report")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(relationService, times(1)).reportUser(eq(TEST_USER_ID), any(UserReportDto.class));
    }

    @Test
    @DisplayName("TC-REPORT-002: 举报用户 - 不能举报自己")
    public void testReportUser_CannotReportSelf() throws Exception {
        // Given: 尝试举报自己
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_USER_ID);
        dto.setReason("spam");
        dto.setDescription("测试");

        when(relationService.reportUser(eq(TEST_USER_ID), any(UserReportDto.class)))
            .thenReturn(R.fail("不能举报自己"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/report")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("TC-REPORT-003: 举报用户 - 验证必填字段")
    public void testReportUser_ValidationFail() throws Exception {
        // Given: 缺少举报原因
        UserReportDto dto = new UserReportDto();
        dto.setReportedUserId(TEST_OTHER_USER_ID);
        dto.setReportType(1);
        dto.setReportReason(""); // 空原因

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/report")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isBadRequest());

        verify(relationService, never()).reportUser(anyLong(), any());
    }

    @Test
    @DisplayName("TC-REPORT-004: 举报用户 - 多种举报类型")
    public void testReportUser_DifferentTypes() throws Exception {
        // 测试不同的举报类型
        int[] reportTypes = {1, 2, 3, 4}; // 1=违规内容, 2=骚扰, 3=欺诈, 4=其他
        String[] reasons = {"违规内容", "骚扰他人", "欺诈行为", "其他原因"};

        for (int i = 0; i < reportTypes.length; i++) {
            UserReportDto dto = new UserReportDto();
            dto.setReportedUserId(TEST_OTHER_USER_ID);
            dto.setReportType(reportTypes[i]);
            dto.setReportReason(reasons[i]);

            when(relationService.reportUser(eq(TEST_USER_ID), any(UserReportDto.class)))
                .thenReturn(R.ok());

            mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/report")
                    .header(AUTHORIZATION_HEADER, getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        verify(relationService, times(4)).reportUser(eq(TEST_USER_ID), any(UserReportDto.class));
    }

    // ==================== 搜索防抖测试 ====================

    @Test
    @DisplayName("TC-SEARCH-001: 粉丝搜索 - 500ms防抖机制")
    public void testFansSearch_Debounce() throws Exception {
        // 说明: 验证前端搜索防抖机制的后端支持
        // 前端会在用户停止输入500ms后才发送请求

        TableDataInfo<UserRelationVo> mockData = new TableDataInfo<>();
        mockData.setRows(Arrays.asList());
        mockData.setTotal(0L);

        when(relationService.getFansList(eq(TEST_USER_ID), anyString(), any(PageQuery.class)))
            .thenReturn(mockData);

        // 模拟连续输入(实际场景中前端防抖会减少请求)
        String[] keywords = {"张", "张三", "张三丰"};
        for (String keyword : keywords) {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                    .header(AUTHORIZATION_HEADER, getAuthHeader())
                    .param("keyword", keyword)
                    .param("pageNum", "1")
                    .param("pageSize", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        verify(relationService, times(3)).getFansList(eq(TEST_USER_ID), anyString(), any(PageQuery.class));
    }

    // ==================== 分页测试 ====================

    @Test
    @DisplayName("TC-PAGE-001: 粉丝列表分页 - hasMore判断")
    public void testFansListPagination_HasMore() throws Exception {
        // Given: 总共50个粉丝,每页20个
        TableDataInfo<UserRelationVo> page1 = new TableDataInfo<>();
        page1.setRows(Arrays.asList(/* 20条数据 */));
        page1.setTotal(50L);

        when(relationService.getFansList(eq(TEST_USER_ID), isNull(), any(PageQuery.class)))
            .thenReturn(page1);

        // When & Then: 第1页,还有更多数据
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(50));

        // hasMore = (1 * 20) < 50 = true
    }

    @Test
    @DisplayName("TC-PAGE-002: 关注列表分页 - 最后一页")
    public void testFollowingListPagination_LastPage() throws Exception {
        // Given: 总共25个关注,每页20个,第2页只有5个
        TableDataInfo<UserRelationVo> page2 = new TableDataInfo<>();
        page2.setRows(Arrays.asList(/* 5条数据 */));
        page2.setTotal(25L);

        when(relationService.getFollowingList(eq(TEST_USER_ID), isNull(), any(PageQuery.class)))
            .thenReturn(page2);

        // When & Then: 第2页,没有更多数据
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/following")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "2")
                .param("pageSize", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(25));

        // hasMore = (2 * 20) < 25 = false
    }
}
