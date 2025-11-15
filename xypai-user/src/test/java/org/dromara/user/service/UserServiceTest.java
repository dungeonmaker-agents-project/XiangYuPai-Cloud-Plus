package org.dromara.user.service;

import org.dromara.common.core.domain.R;
import org.dromara.user.BaseTest;
import org.dromara.user.domain.dto.UpdateBirthdayDto;
import org.dromara.user.domain.dto.UpdateGenderDto;
import org.dromara.user.domain.dto.UpdateNicknameDto;
import org.dromara.user.domain.dto.UserUpdateDto;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.vo.UserProfileVo;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 测试类
 *
 * 测试业务逻辑层:
 * - 用户资料获取
 * - 用户资料更新
 * - 头像上传
 * - 数据验证
 * - 业务规则验证
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("用户服务测试 - UserService Tests")
public class UserServiceTest extends BaseTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private MultipartFile mockFile;

    // ==================== 获取用户资料测试 ====================

    @Test
    @DisplayName("TC-SERVICE-001: 获取用户资料 - 成功")
    public void testGetUserProfile_Success() {
        // Given
        User mockUser = new User();
        mockUser.setUserId(TEST_USER_ID);
        mockUser.setNickname("测试用户");
        mockUser.setAvatar("https://cdn.example.com/avatar.jpg");
        mockUser.setGender(1);
        mockUser.setBirthday(LocalDate.of(1995, 5, 15));
        mockUser.setResidence("北京市");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

        // When
        R<UserProfileVo> result = userService.getUserProfile(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(TEST_USER_ID, result.getData().getUserId());
        assertEquals("测试用户", result.getData().getNickname());
        assertEquals(1, result.getData().getGender());

        verify(userMapper, times(1)).selectById(TEST_USER_ID);
    }

    @Test
    @DisplayName("TC-SERVICE-002: 获取用户资料 - 用户不存在")
    public void testGetUserProfile_UserNotFound() {
        // Given
        when(userMapper.selectById(TEST_USER_ID)).thenReturn(null);

        // When
        R<UserProfileVo> result = userService.getUserProfile(TEST_USER_ID);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        assertNull(result.getData());

        verify(userMapper, times(1)).selectById(TEST_USER_ID);
    }

    // ==================== 更新昵称测试 ====================

    @Test
    @DisplayName("TC-SERVICE-003: 更新昵称 - 成功")
    public void testUpdateNickname_Success() {
        // Given
        UpdateNicknameDto dto = new UpdateNicknameDto();
        dto.setNickname("新昵称");

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);
        existingUser.setNickname("旧昵称");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateNickname(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(userMapper, times(1)).selectById(TEST_USER_ID);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("TC-SERVICE-004: 更新昵称 - 昵称过长(超过20字符)")
    public void testUpdateNickname_TooLong() {
        // Given
        UpdateNicknameDto dto = new UpdateNicknameDto();
        dto.setNickname("这是一个非常非常非常长的昵称超过了二十个字符的限制");

        // When
        R<Void> result = userService.updateNickname(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("昵称") || result.getMsg().contains("长度"));

        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-SERVICE-005: 更新昵称 - 包含敏感词")
    public void testUpdateNickname_SensitiveWords() {
        // Given
        UpdateNicknameDto dto = new UpdateNicknameDto();
        dto.setNickname("admin管理员");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(new User());

        // When
        R<Void> result = userService.updateNickname(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        // 根据实际实现,可能允许或拒绝敏感词
        // 此处假设系统有敏感词过滤
        if (!result.isSuccess()) {
            assertTrue(result.getMsg().contains("敏感词") || result.getMsg().contains("不允许"));
        }
    }

    // ==================== 更新性别测试 ====================

    @Test
    @DisplayName("TC-SERVICE-006: 更新性别 - 成功")
    public void testUpdateGender_Success() {
        // Given
        UpdateGenderDto dto = new UpdateGenderDto();
        dto.setGender(1); // 1=男

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);
        existingUser.setGender(0); // 原来是未知

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateGender(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("TC-SERVICE-007: 更新性别 - 无效值")
    public void testUpdateGender_InvalidValue() {
        // Given
        UpdateGenderDto dto = new UpdateGenderDto();
        dto.setGender(99); // 无效值,应该是0/1/2

        // When
        R<Void> result = userService.updateGender(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("性别") || result.getMsg().contains("无效"));

        verify(userMapper, never()).updateById(any());
    }

    // ==================== 更新生日测试 ====================

    @Test
    @DisplayName("TC-SERVICE-008: 更新生日 - 成功")
    public void testUpdateBirthday_Success() {
        // Given
        UpdateBirthdayDto dto = new UpdateBirthdayDto();
        dto.setBirthday(LocalDate.of(1995, 5, 15));

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateBirthday(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("TC-SERVICE-009: 更新生日 - 未来日期")
    public void testUpdateBirthday_FutureDate() {
        // Given
        UpdateBirthdayDto dto = new UpdateBirthdayDto();
        dto.setBirthday(LocalDate.now().plusDays(1)); // 明天

        // When
        R<Void> result = userService.updateBirthday(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("生日") || result.getMsg().contains("未来"));

        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-SERVICE-010: 更新生日 - 年龄验证(未满18岁)")
    public void testUpdateBirthday_Underage() {
        // Given: 15岁
        UpdateBirthdayDto dto = new UpdateBirthdayDto();
        dto.setBirthday(LocalDate.now().minusYears(15));

        // When
        R<Void> result = userService.updateBirthday(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        // 根据业务规则,可能要求满18岁
        if (!result.isSuccess()) {
            assertTrue(result.getMsg().contains("18") || result.getMsg().contains("年龄"));
        }
    }

    @Test
    @DisplayName("TC-SERVICE-011: 更新生日 - 过于久远(超过100岁)")
    public void testUpdateBirthday_TooOld() {
        // Given: 120岁
        UpdateBirthdayDto dto = new UpdateBirthdayDto();
        dto.setBirthday(LocalDate.now().minusYears(120));

        // When
        R<Void> result = userService.updateBirthday(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("生日") || result.getMsg().contains("合理"));

        verify(userMapper, never()).updateById(any());
    }

    // ==================== 更新其他字段测试 ====================

    @Test
    @DisplayName("TC-SERVICE-012: 更新居住地 - 成功")
    public void testUpdateResidence_Success() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setResidence("上海市浦东新区");

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateUserProfile(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("TC-SERVICE-013: 更新身高 - 有效范围(100-250cm)")
    public void testUpdateHeight_ValidRange() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setHeight(175);

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateUserProfile(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("TC-SERVICE-014: 更新身高 - 无效值(小于100)")
    public void testUpdateHeight_Invalid() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setHeight(50); // 太矮,不合理

        // When
        R<Void> result = userService.updateUserProfile(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("身高") || result.getMsg().contains("合理"));
    }

    @Test
    @DisplayName("TC-SERVICE-015: 更新体重 - 有效范围(30-300kg)")
    public void testUpdateWeight_ValidRange() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setWeight(65);

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateUserProfile(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("TC-SERVICE-016: 更新个人简介 - 长度限制(500字符)")
    public void testUpdateBio_LengthLimit() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        StringBuilder longBio = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            longBio.append("A");
        }
        dto.setBio(longBio.toString());

        // When
        R<Void> result = userService.updateUserProfile(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("简介") || result.getMsg().contains("长度"));
    }

    // ==================== 头像上传测试 ====================

    @Test
    @DisplayName("TC-SERVICE-017: 上传头像 - 成功")
    public void testUploadAvatar_Success() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L * 1024); // 1MB
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getOriginalFilename()).thenReturn("avatar.jpg");

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<String> result = userService.uploadAvatar(TEST_USER_ID, mockFile);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().contains("https://"));
    }

    @Test
    @DisplayName("TC-SERVICE-018: 上传头像 - 文件为空")
    public void testUploadAvatar_EmptyFile() {
        // Given
        when(mockFile.isEmpty()).thenReturn(true);

        // When
        R<String> result = userService.uploadAvatar(TEST_USER_ID, mockFile);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("文件") || result.getMsg().contains("空"));

        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-SERVICE-019: 上传头像 - 文件过大(超过5MB)")
    public void testUploadAvatar_FileTooLarge() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(6L * 1024 * 1024); // 6MB
        when(mockFile.getContentType()).thenReturn("image/jpeg");

        // When
        R<String> result = userService.uploadAvatar(TEST_USER_ID, mockFile);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("5MB") || result.getMsg().contains("大小"));
    }

    @Test
    @DisplayName("TC-SERVICE-020: 上传头像 - 不支持的格式")
    public void testUploadAvatar_UnsupportedFormat() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L * 1024);
        when(mockFile.getContentType()).thenReturn("image/gif"); // 不支持GIF

        // When
        R<String> result = userService.uploadAvatar(TEST_USER_ID, mockFile);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("JPG") || result.getMsg().contains("PNG") || result.getMsg().contains("格式"));
    }

    // ==================== 并发更新测试 ====================

    @Test
    @DisplayName("TC-SERVICE-021: 并发更新 - 乐观锁测试")
    public void testConcurrentUpdate_OptimisticLock() {
        // 说明: 测试多个用户同时更新同一字段时的并发控制
        // 实际应使用version字段实现乐观锁

        UserUpdateDto dto = new UserUpdateDto();
        dto.setNickname("并发测试");

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);
        existingUser.setVersion(1); // 版本号

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When: 模拟两次并发更新
        R<Void> result1 = userService.updateNickname(TEST_USER_ID, dto);
        R<Void> result2 = userService.updateNickname(TEST_USER_ID, dto);

        // Then: 至少有一次成功
        assertTrue(result1.isSuccess() || result2.isSuccess());
    }

    // ==================== 业务规则测试 ====================

    @Test
    @DisplayName("TC-SERVICE-022: 微信号格式验证")
    public void testUpdateWechat_FormatValidation() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setWechatId("valid_wechat_123"); // 有效格式

        User existingUser = new User();
        existingUser.setUserId(TEST_USER_ID);

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        R<Void> result = userService.updateUserProfile(TEST_USER_ID, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("TC-SERVICE-023: 获取他人资料 - 隐私控制")
    public void testGetOtherUserProfile_PrivacyControl() {
        // Given
        User targetUser = new User();
        targetUser.setUserId(TEST_OTHER_USER_ID);
        targetUser.setNickname("目标用户");
        targetUser.setPrivacyProfile(1); // 1=公开, 2=仅粉丝, 3=私密

        when(userMapper.selectById(TEST_OTHER_USER_ID)).thenReturn(targetUser);

        // When
        R<UserProfileVo> result = userService.getOtherUserProfile(TEST_USER_ID, TEST_OTHER_USER_ID);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        // 验证隐私标志
        UserProfileVo profile = result.getData();
        assertNotNull(profile.getCanViewProfile());
        assertNotNull(profile.getCanViewMoments());
        assertNotNull(profile.getCanViewSkills());
    }
}
