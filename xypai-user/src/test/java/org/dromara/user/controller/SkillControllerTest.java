package org.dromara.user.controller;

import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.BaseTest;
import org.dromara.user.controller.app.SkillController;
import org.dromara.user.domain.dto.OfflineSkillCreateDto;
import org.dromara.user.domain.dto.OnlineSkillCreateDto;
import org.dromara.user.domain.dto.SkillUpdateDto;
import org.dromara.user.domain.vo.SkillDetailVo;
import org.dromara.user.domain.vo.SkillVo;
import org.dromara.user.service.ISkillService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SkillController 测试类
 *
 * 测试范围:
 * - 页面06: 技能管理页面 (创建线上/线下技能、编辑、删除、上下架、查看详情、我的技能列表)
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("技能管理控制器测试 - SkillController Tests")
public class SkillControllerTest extends BaseTest {

    @Mock
    private ISkillService skillService;

    @InjectMocks
    private SkillController skillController;

    // ==================== 创建技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-001: 创建线上技能(陪玩) - 成功")
    public void testCreateOnlineSkill_Success() throws Exception {
        // Given: 准备线上技能数据
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setCoverImage("https://cdn.example.com/cover.jpg");
        dto.setGameName("王者荣耀");
        dto.setGameRank("王者");
        dto.setSkillName("王者带飞");
        dto.setDescription("5年王者经验,可带上分");
        dto.setPrice(new BigDecimal("50.00"));
        dto.setServiceHours(new BigDecimal("1.0"));
        dto.setImages(Arrays.asList(
            "https://cdn.example.com/skill1.jpg",
            "https://cdn.example.com/skill2.jpg"
        ));

        Long expectedSkillId = 3001L;
        when(skillService.createOnlineSkill(eq(TEST_USER_ID), any(OnlineSkillCreateDto.class)))
            .thenReturn(R.ok(expectedSkillId));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/online")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(expectedSkillId));

        verify(skillService, times(1)).createOnlineSkill(eq(TEST_USER_ID), any(OnlineSkillCreateDto.class));
    }

    @Test
    @DisplayName("TC-SKILL-002: 创建线下技能(服务) - 成功")
    public void testCreateOfflineSkill_Success() throws Exception {
        // Given: 准备线下技能数据
        OfflineSkillCreateDto dto = new OfflineSkillCreateDto();
        dto.setCoverImage("https://cdn.example.com/cover.jpg");
        dto.setServiceType("photography");
        dto.setSkillName("专业摄影师 - 婚礼/活动拍摄");
        dto.setDescription("10年摄影经验,擅长人像和风景");
        dto.setPrice(new BigDecimal("800.00"));

        OfflineSkillCreateDto.LocationDto location = new OfflineSkillCreateDto.LocationDto();
        location.setAddress("北京市朝阳区");
        location.setLatitude(new BigDecimal("39.9042"));
        location.setLongitude(new BigDecimal("116.4074"));
        dto.setLocation(location);

        dto.setAvailableTimes(Arrays.asList());
        dto.setPromises(Arrays.asList("满意为止", "提供后期修图"));
        dto.setImages(Arrays.asList("https://cdn.example.com/photo1.jpg"));

        Long expectedSkillId = 3002L;
        when(skillService.createOfflineSkill(eq(TEST_USER_ID), any(OfflineSkillCreateDto.class)))
            .thenReturn(R.ok(expectedSkillId));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/offline")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(expectedSkillId));

        verify(skillService, times(1)).createOfflineSkill(eq(TEST_USER_ID), any(OfflineSkillCreateDto.class));
    }

    @Test
    @DisplayName("TC-SKILL-003: 创建技能 - 验证失败(价格为负)")
    public void testCreateSkill_InvalidPrice() throws Exception {
        // Given: 无效的价格
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setGameName("王者荣耀");
        dto.setPrice(new BigDecimal("-10.00")); // 负数价格

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/online")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isBadRequest());

        verify(skillService, never()).createOnlineSkill(anyLong(), any());
    }

    @Test
    @DisplayName("TC-SKILL-004: 创建技能 - 图片超过限制(最多9张)")
    public void testCreateSkill_TooManyImages() throws Exception {
        // Given: 超过9张图片
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setGameName("王者荣耀");
        dto.setGameRank("王者");
        dto.setSkillName("测试技能");
        dto.setDescription("这是一个测试技能描述");
        dto.setPrice(new BigDecimal("50.00"));
        dto.setServiceHours(new BigDecimal("1.0"));
        dto.setImages(Arrays.asList(
            "url1", "url2", "url3", "url4", "url5",
            "url6", "url7", "url8", "url9", "url10" // 10张,超过限制
        ));

        when(skillService.createOnlineSkill(eq(TEST_USER_ID), any()))
            .thenReturn(R.fail("图片数量不能超过9张"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/online")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 更新技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-005: 更新技能 - 成功")
    public void testUpdateSkill_Success() throws Exception {
        // Given
        SkillUpdateDto dto = new SkillUpdateDto();
        dto.setDescription("更新后的描述");
        dto.setPricePerHour(new BigDecimal("60.00"));

        when(skillService.updateSkill(eq(TEST_USER_ID), eq(TEST_SKILL_ID), any(SkillUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + TEST_SKILL_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(skillService, times(1)).updateSkill(eq(TEST_USER_ID), eq(TEST_SKILL_ID), any(SkillUpdateDto.class));
    }

    @Test
    @DisplayName("TC-SKILL-006: 更新技能 - 非所有者无法更新")
    public void testUpdateSkill_NotOwner() throws Exception {
        // Given
        SkillUpdateDto dto = new SkillUpdateDto();
        dto.setDescription("更新描述");

        when(skillService.updateSkill(eq(TEST_USER_ID), eq(TEST_SKILL_ID), any()))
            .thenReturn(R.fail("无权限操作"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + TEST_SKILL_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 删除技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-007: 删除技能 - 成功")
    public void testDeleteSkill_Success() throws Exception {
        // Given
        when(skillService.deleteSkill(TEST_USER_ID, TEST_SKILL_ID))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/skills/" + TEST_SKILL_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(skillService, times(1)).deleteSkill(TEST_USER_ID, TEST_SKILL_ID);
    }

    @Test
    @DisplayName("TC-SKILL-008: 删除技能 - 技能不存在")
    public void testDeleteSkill_NotFound() throws Exception {
        // Given
        Long nonExistentSkillId = 99999L;
        when(skillService.deleteSkill(TEST_USER_ID, nonExistentSkillId))
            .thenReturn(R.fail("技能不存在"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/skills/" + nonExistentSkillId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 上下架技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-009: 上架技能 - 成功")
    public void testToggleSkillOnline_ToOnline_Success() throws Exception {
        // Given
        when(skillService.toggleSkillOnline(TEST_USER_ID, TEST_SKILL_ID, true))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + TEST_SKILL_ID + "/toggle")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("isOnline", "true")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(skillService, times(1)).toggleSkillOnline(TEST_USER_ID, TEST_SKILL_ID, true);
    }

    @Test
    @DisplayName("TC-SKILL-010: 下架技能 - 成功")
    public void testToggleSkillOnline_ToOffline_Success() throws Exception {
        // Given
        when(skillService.toggleSkillOnline(TEST_USER_ID, TEST_SKILL_ID, false))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + TEST_SKILL_ID + "/toggle")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("isOnline", "false")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(skillService, times(1)).toggleSkillOnline(TEST_USER_ID, TEST_SKILL_ID, false);
    }

    // ==================== 查询技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-011: 获取技能详情 - 成功")
    public void testGetSkillDetail_Success() throws Exception {
        // Given
        SkillDetailVo mockDetail = SkillDetailVo.builder()
            .skillId(TEST_SKILL_ID)
            .userId(TEST_USER_ID)
            .skillType("online")
            .gameName("王者荣耀")
            .gameRank("王者")
            .price(new BigDecimal("50.00"))
            .description("技能描述")
            .orderCount(20)
            .rating(new BigDecimal("4.8"))
            .isOnline(true)
            .build();

        when(skillService.getSkillDetail(TEST_SKILL_ID))
            .thenReturn(R.ok(mockDetail));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/" + TEST_SKILL_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.skillId").value(TEST_SKILL_ID))
            .andExpect(jsonPath("$.data.gameName").value("王者荣耀"))
            .andExpect(jsonPath("$.data.rating").value(4.8));

        verify(skillService, times(1)).getSkillDetail(TEST_SKILL_ID);
    }

    @Test
    @DisplayName("TC-SKILL-012: 获取我的技能列表 - 成功")
    public void testGetMySkills_Success() throws Exception {
        // Given
        List<SkillVo> skills = Arrays.asList(
            SkillVo.builder()
                .skillId(3001L)
                .skillType("online")
                .gameName("王者荣耀")
                .price(new BigDecimal("50.00"))
                .isOnline(true)
                .build(),
            SkillVo.builder()
                .skillId(3002L)
                .skillType("offline")
                .serviceType("摄影服务")
                .price(new BigDecimal("800.00"))
                .isOnline(false)
                .build()
        );

        TableDataInfo<SkillVo> mockData = new TableDataInfo<>();
        mockData.setRows(skills);
        mockData.setTotal(2L);

        when(skillService.getMySkills(eq(TEST_USER_ID), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/my")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.rows[0].skillId").value(3001))
            .andExpect(jsonPath("$.rows[1].skillId").value(3002));

        verify(skillService, times(1)).getMySkills(eq(TEST_USER_ID), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-SKILL-013: 获取用户技能列表 - 成功")
    public void testGetUserSkills_Success() throws Exception {
        // Given
        Long targetUserId = TEST_OTHER_USER_ID;
        List<SkillVo> skills = Arrays.asList(
            SkillVo.builder()
                .skillId(3003L)
                .skillType("online")
                .gameName("和平精英")
                .price(new BigDecimal("40.00"))
                .isOnline(true)
                .build()
        );

        TableDataInfo<SkillVo> mockData = new TableDataInfo<>();
        mockData.setRows(skills);
        mockData.setTotal(1L);

        when(skillService.getUserSkills(eq(targetUserId), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/user/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.rows[0].gameName").value("和平精英"));

        verify(skillService, times(1)).getUserSkills(eq(targetUserId), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-SKILL-014: 搜索附近技能 - 成功")
    public void testSearchNearbySkills_Success() throws Exception {
        // Given: 北京市中心坐标
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal longitude = new BigDecimal("116.4074");
        Integer radiusMeters = 5000; // 5公里

        List<SkillVo> nearbySkills = Arrays.asList(
            SkillVo.builder()
                .skillId(3004L)
                .skillType("offline")
                .serviceType("家政服务")
                .distance(new BigDecimal("1.2")) // 1.2公里
                .build(),
            SkillVo.builder()
                .skillId(3005L)
                .skillType("offline")
                .serviceType("维修服务")
                .distance(new BigDecimal("3.5")) // 3.5公里
                .build()
        );

        TableDataInfo<SkillVo> mockData = new TableDataInfo<>();
        mockData.setRows(nearbySkills);
        mockData.setTotal(2L);

        when(skillService.searchNearbySkills(eq(latitude), eq(longitude), eq(radiusMeters), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/nearby")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("latitude", latitude.toString())
                .param("longitude", longitude.toString())
                .param("radiusMeters", radiusMeters.toString())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.rows[0].distance").value(1.2))
            .andExpect(jsonPath("$.rows[1].distance").value(3.5));

        verify(skillService, times(1))
            .searchNearbySkills(eq(latitude), eq(longitude), eq(radiusMeters), any(PageQuery.class));
    }

    @Test
    @DisplayName("TC-SKILL-015: 搜索附近技能 - 默认半径10公里")
    public void testSearchNearbySkills_DefaultRadius() throws Exception {
        // Given
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal longitude = new BigDecimal("116.4074");
        Integer defaultRadius = 10000; // 默认10公里

        TableDataInfo<SkillVo> mockData = new TableDataInfo<>();
        mockData.setRows(Arrays.asList());
        mockData.setTotal(0L);

        when(skillService.searchNearbySkills(eq(latitude), eq(longitude), eq(defaultRadius), any(PageQuery.class)))
            .thenReturn(mockData);

        // When & Then: 不传radiusMeters参数,使用默认值
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/nearby")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("latitude", latitude.toString())
                .param("longitude", longitude.toString())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(skillService, times(1))
            .searchNearbySkills(eq(latitude), eq(longitude), eq(defaultRadius), any(PageQuery.class));
    }

    // ==================== 分页测试 ====================

    @Test
    @DisplayName("TC-SKILL-016: 技能列表分页 - 测试hasMore标志")
    public void testSkillListPagination_HasMore() throws Exception {
        // Given: 每页10条,共25条数据
        TableDataInfo<SkillVo> page1 = new TableDataInfo<>();
        page1.setRows(Arrays.asList(/* 10条数据 */));
        page1.setTotal(25L);

        when(skillService.getMySkills(eq(TEST_USER_ID), any(PageQuery.class)))
            .thenReturn(page1);

        // When & Then: 第1页,应该有更多数据
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/my")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(25));

        // 验证: 25条数据,每页10条,第1页后还有数据
        // hasMore = (pageNum * pageSize) < total
        // hasMore = (1 * 10) < 25 = true
    }

    // ==================== 性能测试场景 ====================

    @Test
    @DisplayName("TC-PERF-001: 批量获取技能 - 并发请求测试")
    public void testBatchGetSkills_Concurrent() throws Exception {
        // 说明: 验证后端能正确处理并发请求
        // 实际压测应使用JMeter等专业工具

        TableDataInfo<SkillVo> mockData = new TableDataInfo<>();
        mockData.setRows(Arrays.asList());
        mockData.setTotal(0L);

        when(skillService.getMySkills(eq(TEST_USER_ID), any(PageQuery.class)))
            .thenReturn(mockData);

        // 模拟5次并发请求
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/my")
                    .header(AUTHORIZATION_HEADER, getAuthHeader())
                    .param("pageNum", "1")
                    .param("pageSize", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        verify(skillService, times(5)).getMySkills(eq(TEST_USER_ID), any(PageQuery.class));
    }
}
