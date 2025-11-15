package org.dromara.user.service;

import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.user.BaseTest;
import org.dromara.user.domain.dto.OfflineSkillCreateDto;
import org.dromara.user.domain.dto.OnlineSkillCreateDto;
import org.dromara.user.domain.dto.SkillUpdateDto;
import org.dromara.user.domain.entity.Skill;
import org.dromara.user.domain.vo.SkillDetailVo;
import org.dromara.user.domain.vo.SkillVo;
import org.dromara.user.mapper.SkillMapper;
import org.dromara.user.service.impl.SkillServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SkillService 测试类
 *
 * 测试业务逻辑层:
 * - 线上技能创建(陪玩类)
 * - 线下技能创建(服务类)
 * - 技能更新和删除
 * - 技能上下架
 * - 技能查询(详情、列表、附近)
 * - 数据验证和业务规则
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("技能服务测试 - SkillService Tests")
public class SkillServiceTest extends BaseTest {

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    // ==================== 创建线上技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-001: 创建线上技能 - 成功")
    public void testCreateOnlineSkill_Success() {
        // Given
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setGameName("王者荣耀");
        dto.setRank("王者");
        dto.setPricePerHour(new BigDecimal("50.00"));
        dto.setDescription("5年王者经验");
        dto.setImages(Arrays.asList("https://cdn.example.com/img1.jpg"));

        when(skillMapper.insert(any(Skill.class))).thenReturn(1);

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(skillMapper, times(1)).insert(any(Skill.class));
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-002: 创建线上技能 - 价格验证失败(负数)")
    public void testCreateOnlineSkill_NegativePrice() {
        // Given
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setPricePerHour(new BigDecimal("-10.00")); // 负数价格

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("价格") || result.getMsg().contains("负数"));
        verify(skillMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-003: 创建线上技能 - 价格为0")
    public void testCreateOnlineSkill_ZeroPrice() {
        // Given
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setPricePerHour(BigDecimal.ZERO);

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("价格"));
        verify(skillMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-004: 创建线上技能 - 图片数量超过限制(>9)")
    public void testCreateOnlineSkill_TooManyImages() {
        // Given
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setPricePerHour(new BigDecimal("50.00"));
        dto.setImages(Arrays.asList(
            "url1", "url2", "url3", "url4", "url5",
            "url6", "url7", "url8", "url9", "url10" // 10张,超过限制
        ));

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("图片") && result.getMsg().contains("9"));
        verify(skillMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-005: 创建线上技能 - 游戏信息验证")
    public void testCreateOnlineSkill_GameValidation() {
        // Given: 缺少游戏名称
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setGameName(""); // 空游戏名
        dto.setPricePerHour(new BigDecimal("50.00"));

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("游戏"));
    }

    // ==================== 创建线下技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-006: 创建线下技能 - 成功")
    public void testCreateOfflineSkill_Success() {
        // Given
        OfflineSkillCreateDto dto = new OfflineSkillCreateDto();
        dto.setSkillType(2);
        dto.setServiceTypeId("photography");
        dto.setServiceTypeName("摄影服务");
        dto.setTitle("专业摄影师");
        dto.setPricePerService(new BigDecimal("800.00"));
        dto.setDescription("10年经验");
        dto.setServiceLocation("北京市朝阳区");
        dto.setLatitude(new BigDecimal("39.9042"));
        dto.setLongitude(new BigDecimal("116.4074"));
        dto.setAvailableTimes(Arrays.asList("周末"));
        dto.setPromises(Arrays.asList("满意为止"));
        dto.setImages(Arrays.asList("https://cdn.example.com/photo.jpg"));

        when(skillMapper.insert(any(Skill.class))).thenReturn(1);

        // When
        R<Long> result = skillService.createOfflineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(skillMapper, times(1)).insert(any(Skill.class));
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-007: 创建线下技能 - 地理位置验证")
    public void testCreateOfflineSkill_LocationValidation() {
        // Given: 无效的经纬度
        OfflineSkillCreateDto dto = new OfflineSkillCreateDto();
        dto.setSkillType(2);
        dto.setServiceTypeId("photography");
        dto.setPricePerService(new BigDecimal("800.00"));
        dto.setServiceLocation("北京市");
        dto.setLatitude(new BigDecimal("200.0")); // 无效纬度(>90)
        dto.setLongitude(new BigDecimal("116.4074"));

        // When
        R<Long> result = skillService.createOfflineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("纬度") || result.getMsg().contains("经度"));
        verify(skillMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-008: 创建线下技能 - 服务承诺验证")
    public void testCreateOfflineSkill_PromisesValidation() {
        // Given: 服务承诺过多(超过5条)
        OfflineSkillCreateDto dto = new OfflineSkillCreateDto();
        dto.setSkillType(2);
        dto.setServiceTypeId("photography");
        dto.setPricePerService(new BigDecimal("800.00"));
        dto.setPromises(Arrays.asList(
            "承诺1", "承诺2", "承诺3", "承诺4", "承诺5", "承诺6" // 6条,超过限制
        ));

        // When
        R<Long> result = skillService.createOfflineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("承诺") || result.getMsg().contains("5"));
    }

    // ==================== 更新技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-009: 更新技能 - 成功")
    public void testUpdateSkill_Success() {
        // Given
        SkillUpdateDto dto = new SkillUpdateDto();
        dto.setDescription("更新后的描述");
        dto.setPricePerHour(new BigDecimal("60.00"));

        Skill existingSkill = new Skill();
        existingSkill.setSkillId(TEST_SKILL_ID);
        existingSkill.setUserId(TEST_USER_ID);
        existingSkill.setSkillType(1);

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);
        when(skillMapper.updateById(any(Skill.class))).thenReturn(1);

        // When
        R<Void> result = skillService.updateSkill(TEST_USER_ID, TEST_SKILL_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(skillMapper, times(1)).updateById(any(Skill.class));
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-010: 更新技能 - 非所有者无法更新")
    public void testUpdateSkill_NotOwner() {
        // Given
        SkillUpdateDto dto = new SkillUpdateDto();
        dto.setDescription("更新描述");

        Skill existingSkill = new Skill();
        existingSkill.setSkillId(TEST_SKILL_ID);
        existingSkill.setUserId(TEST_OTHER_USER_ID); // 其他用户的技能

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);

        // When
        R<Void> result = skillService.updateSkill(TEST_USER_ID, TEST_SKILL_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("权限") || result.getMsg().contains("无法"));
        verify(skillMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-011: 更新技能 - 技能不存在")
    public void testUpdateSkill_NotFound() {
        // Given
        SkillUpdateDto dto = new SkillUpdateDto();
        dto.setDescription("更新描述");

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(null);

        // When
        R<Void> result = skillService.updateSkill(TEST_USER_ID, TEST_SKILL_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        verify(skillMapper, never()).updateById(any());
    }

    // ==================== 删除技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-012: 删除技能 - 成功")
    public void testDeleteSkill_Success() {
        // Given
        Skill existingSkill = new Skill();
        existingSkill.setSkillId(TEST_SKILL_ID);
        existingSkill.setUserId(TEST_USER_ID);

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);
        when(skillMapper.deleteById(TEST_SKILL_ID)).thenReturn(1);

        // When
        R<Void> result = skillService.deleteSkill(TEST_USER_ID, TEST_SKILL_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(skillMapper, times(1)).deleteById(TEST_SKILL_ID);
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-013: 删除技能 - 非所有者无法删除")
    public void testDeleteSkill_NotOwner() {
        // Given
        Skill existingSkill = new Skill();
        existingSkill.setSkillId(TEST_SKILL_ID);
        existingSkill.setUserId(TEST_OTHER_USER_ID);

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);

        // When
        R<Void> result = skillService.deleteSkill(TEST_USER_ID, TEST_SKILL_ID);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        verify(skillMapper, never()).deleteById(any());
    }

    // ==================== 上下架技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-014: 上架技能 - 成功")
    public void testToggleSkillOnline_ToOnline() {
        // Given
        Skill existingSkill = new Skill();
        existingSkill.setSkillId(TEST_SKILL_ID);
        existingSkill.setUserId(TEST_USER_ID);
        existingSkill.setIsOnline(false); // 当前下架

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);
        when(skillMapper.updateById(any(Skill.class))).thenReturn(1);

        // When
        R<Void> result = skillService.toggleSkillOnline(TEST_USER_ID, TEST_SKILL_ID, true);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(skillMapper, times(1)).updateById(any(Skill.class));
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-015: 下架技能 - 成功")
    public void testToggleSkillOnline_ToOffline() {
        // Given
        Skill existingSkill = new Skill();
        existingSkill.setSkillId(TEST_SKILL_ID);
        existingSkill.setUserId(TEST_USER_ID);
        existingSkill.setIsOnline(true); // 当前上架

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);
        when(skillMapper.updateById(any(Skill.class))).thenReturn(1);

        // When
        R<Void> result = skillService.toggleSkillOnline(TEST_USER_ID, TEST_SKILL_ID, false);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(skillMapper, times(1)).updateById(any(Skill.class));
    }

    // ==================== 查询技能测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-016: 获取技能详情 - 成功")
    public void testGetSkillDetail_Success() {
        // Given
        Skill skill = new Skill();
        skill.setSkillId(TEST_SKILL_ID);
        skill.setUserId(TEST_USER_ID);
        skill.setSkillType(1);
        skill.setGameName("王者荣耀");
        skill.setPricePerHour(new BigDecimal("50.00"));

        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(skill);

        // When
        R<SkillDetailVo> result = skillService.getSkillDetail(TEST_SKILL_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(TEST_SKILL_ID, result.getData().getSkillId());
        verify(skillMapper, times(1)).selectById(TEST_SKILL_ID);
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-017: 获取技能详情 - 技能不存在")
    public void testGetSkillDetail_NotFound() {
        // Given
        when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(null);

        // When
        R<SkillDetailVo> result = skillService.getSkillDetail(TEST_SKILL_ID);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-018: 获取我的技能列表 - 成功")
    public void testGetMySkills_Success() {
        // Given
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        when(skillMapper.selectMySkills(eq(TEST_USER_ID), any()))
            .thenReturn(Arrays.asList());

        // When
        TableDataInfo<SkillVo> result = skillService.getMySkills(TEST_USER_ID, pageQuery);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRows());
        verify(skillMapper, times(1)).selectMySkills(eq(TEST_USER_ID), any());
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-019: 搜索附近技能 - 成功")
    public void testSearchNearbySkills_Success() {
        // Given
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal longitude = new BigDecimal("116.4074");
        Integer radiusMeters = 5000;
        PageQuery pageQuery = new PageQuery();

        when(skillMapper.selectNearbySkills(eq(latitude), eq(longitude), eq(radiusMeters), any()))
            .thenReturn(Arrays.asList());

        // When
        TableDataInfo<SkillVo> result = skillService.searchNearbySkills(
            latitude, longitude, radiusMeters, pageQuery
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getRows());
        verify(skillMapper, times(1)).selectNearbySkills(
            eq(latitude), eq(longitude), eq(radiusMeters), any()
        );
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-020: 搜索附近技能 - 半径过大")
    public void testSearchNearbySkills_RadiusTooLarge() {
        // Given: 半径超过50公里
        BigDecimal latitude = new BigDecimal("39.9042");
        BigDecimal longitude = new BigDecimal("116.4074");
        Integer radiusMeters = 100000; // 100公里,超过限制
        PageQuery pageQuery = new PageQuery();

        // When
        TableDataInfo<SkillVo> result = skillService.searchNearbySkills(
            latitude, longitude, radiusMeters, pageQuery
        );

        // Then
        assertNotNull(result);
        // 应该使用最大限制值50公里,或返回错误
        verify(skillMapper, never()).selectNearbySkills(
            eq(latitude), eq(longitude), eq(radiusMeters), any()
        );
    }

    // ==================== 业务规则测试 ====================

    @Test
    @DisplayName("TC-SKILL-SERVICE-021: 价格精度验证 - 最多2位小数")
    public void testPricePrecision() {
        // Given: 价格有3位小数
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setPricePerHour(new BigDecimal("50.123")); // 3位小数

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("小数") || result.getMsg().contains("精度"));
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-022: 描述长度验证 - 最多500字符")
    public void testDescriptionLength() {
        // Given: 描述超过500字符
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setPricePerHour(new BigDecimal("50.00"));

        StringBuilder longDesc = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longDesc.append("A");
        }
        dto.setDescription(longDesc.toString());

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("描述") || result.getMsg().contains("长度"));
    }

    @Test
    @DisplayName("TC-SKILL-SERVICE-023: 同一用户不能创建过多技能")
    public void testMaxSkillsPerUser() {
        // Given: 用户已有10个技能
        OnlineSkillCreateDto dto = new OnlineSkillCreateDto();
        dto.setSkillType(1);
        dto.setGameId("wzry");
        dto.setPricePerHour(new BigDecimal("50.00"));

        when(skillMapper.countByUserId(TEST_USER_ID)).thenReturn(10L); // 已有10个

        // When
        R<Long> result = skillService.createOnlineSkill(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("数量") || result.getMsg().contains("限制"));
    }
}
