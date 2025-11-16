package org.dromara.user.controller;

import org.dromara.common.core.domain.R;
import org.dromara.user.BaseTest;
import org.dromara.user.controller.app.ProfileController;
import org.dromara.user.domain.dto.UpdateBirthdayDto;
import org.dromara.user.domain.dto.UpdateGenderDto;
import org.dromara.user.domain.dto.UpdateNicknameDto;
import org.dromara.user.domain.dto.UserUpdateDto;
import org.dromara.user.domain.vo.PrivacyVo;
import org.dromara.user.domain.vo.UserProfileVo;
import org.dromara.user.domain.vo.UserStatsVo;
import org.dromara.user.service.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProfileController 测试类
 *
 * 测试范围:
 * - 页面01: 个人主页 (获取个人资料头部信息、他人主页)
 * - 页面02: 编辑资料 (获取编辑数据、更新各项字段、上传头像)
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("用户资料控制器测试 - ProfileController Tests")
public class ProfileControllerTest extends BaseTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private ProfileController profileController;

    // ==================== 页面01: 个人主页测试 ====================

    @Test
    @DisplayName("TC-PROFILE-001: 获取个人主页头部信息 - 成功")
    public void testGetProfileHeader_Success() throws Exception {
        // Given: 准备测试数据
        UserStatsVo stats = UserStatsVo.builder()
            .followingCount(100)
            .fansCount(200)
            .likesCount(300)
            .build();

        UserProfileVo mockProfile = UserProfileVo.builder()
            .userId(TEST_USER_ID)
            .nickname("测试用户")
            .avatar("https://cdn.example.com/avatar.jpg")
            .bio("这是我的个人简介")
            .stats(stats)
            .build();

        when(userService.getUserProfile(TEST_USER_ID)).thenReturn(R.ok(mockProfile));

        // When & Then: 执行请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/header")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").value(TEST_USER_ID))
            .andExpect(jsonPath("$.data.nickname").value("测试用户"))
            .andExpect(jsonPath("$.data.stats.followingCount").value(100))
            .andExpect(jsonPath("$.data.stats.fansCount").value(200));

        verify(userService, times(1)).getUserProfile(TEST_USER_ID);
    }

    @Test
    @DisplayName("TC-PROFILE-002: 获取他人主页 - 成功")
    public void testGetOtherUserProfile_Success() throws Exception {
        // Given
        PrivacyVo privacy = PrivacyVo.builder()
            .canViewProfile(true)
            .canViewMoments(true)
            .canViewSkills(true)
            .build();

        UserProfileVo mockProfile = UserProfileVo.builder()
            .userId(TEST_OTHER_USER_ID)
            .nickname("其他用户")
            .followStatus("none")
            .privacy(privacy)
            .build();

        when(userService.getOtherUserProfile(TEST_USER_ID, TEST_OTHER_USER_ID))
            .thenReturn(R.ok(mockProfile));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + TEST_OTHER_USER_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").value(TEST_OTHER_USER_ID))
            .andExpect(jsonPath("$.data.followStatus").value("none"))
            .andExpect(jsonPath("$.data.privacy.canViewProfile").value(true));

        verify(userService, times(1)).getOtherUserProfile(TEST_USER_ID, TEST_OTHER_USER_ID);
    }

    @Test
    @DisplayName("TC-PROFILE-003: 获取他人主页 - 隐私限制")
    public void testGetOtherUserProfile_PrivacyRestricted() throws Exception {
        // Given: 用户设置了隐私限制
        PrivacyVo privacy = PrivacyVo.builder()
            .canViewProfile(false)
            .canViewMoments(false)
            .canViewSkills(false)
            .build();

        UserProfileVo mockProfile = UserProfileVo.builder()
            .userId(TEST_OTHER_USER_ID)
            .nickname("隐私用户")
            .followStatus("none")
            .privacy(privacy)
            .build();

        when(userService.getOtherUserProfile(TEST_USER_ID, TEST_OTHER_USER_ID))
            .thenReturn(R.ok(mockProfile));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + TEST_OTHER_USER_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.privacy.canViewProfile").value(false))
            .andExpect(jsonPath("$.data.privacy.canViewMoments").value(false))
            .andExpect(jsonPath("$.data.privacy.canViewSkills").value(false));
    }

    // ==================== 页面02: 编辑资料测试 ====================

    @Test
    @DisplayName("TC-EDIT-001: 获取编辑资料数据 - 成功")
    public void testGetProfileEdit_Success() throws Exception {
        // Given
        UserProfileVo mockProfile = UserProfileVo.builder()
            .userId(TEST_USER_ID)
            .nickname("测试用户")
            .gender("male")
            .birthday(LocalDate.of(1995, 5, 15))
            .residence("北京市朝阳区")
            .height(175)
            .weight(65)
            .occupation("软件工程师")
            .wechat("test_wechat")
            .bio("个人简介")
            .build();

        when(userService.getUserProfile(TEST_USER_ID)).thenReturn(R.ok(mockProfile));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.nickname").value("测试用户"))
            .andExpect(jsonPath("$.data.gender").value("male"))
            .andExpect(jsonPath("$.data.height").value(175));
    }

    @Test
    @DisplayName("TC-EDIT-002: 更新昵称 - 成功")
    public void testUpdateNickname_Success() throws Exception {
        // Given
        UpdateNicknameDto dto = new UpdateNicknameDto();
        dto.setNickname("新昵称");

        when(userService.updateNickname(eq(TEST_USER_ID), any(UpdateNicknameDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateNickname(eq(TEST_USER_ID), any(UpdateNicknameDto.class));
    }

    @Test
    @DisplayName("TC-EDIT-003: 更新昵称 - 验证失败(空昵称)")
    public void testUpdateNickname_ValidationFail() throws Exception {
        // Given: 空昵称
        UpdateNicknameDto dto = new UpdateNicknameDto();
        dto.setNickname("");

        // When & Then: 应该返回验证错误
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateNickname(anyLong(), any());
    }

    @Test
    @DisplayName("TC-EDIT-004: 更新性别 - 成功")
    public void testUpdateGender_Success() throws Exception {
        // Given
        UpdateGenderDto dto = new UpdateGenderDto();
        dto.setGender("male"); // male, female, other

        when(userService.updateGender(eq(TEST_USER_ID), any(UpdateGenderDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/gender")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateGender(eq(TEST_USER_ID), any(UpdateGenderDto.class));
    }

    @Test
    @DisplayName("TC-EDIT-005: 更新生日 - 成功")
    public void testUpdateBirthday_Success() throws Exception {
        // Given
        UpdateBirthdayDto dto = new UpdateBirthdayDto();
        dto.setBirthday(LocalDate.of(1995, 5, 15));

        when(userService.updateBirthday(eq(TEST_USER_ID), any(UpdateBirthdayDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/birthday")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateBirthday(eq(TEST_USER_ID), any(UpdateBirthdayDto.class));
    }

    @Test
    @DisplayName("TC-EDIT-006: 更新居住地 - 成功")
    public void testUpdateResidence_Success() throws Exception {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setResidence("上海市浦东新区");

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/residence")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("TC-EDIT-007: 更新身高 - 成功")
    public void testUpdateHeight_Success() throws Exception {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setHeight(180);

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/height")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("TC-EDIT-008: 更新体重 - 成功")
    public void testUpdateWeight_Success() throws Exception {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setWeight(70);

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/weight")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-EDIT-009: 更新职业 - 成功")
    public void testUpdateOccupation_Success() throws Exception {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setOccupation("产品经理");

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/occupation")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-EDIT-010: 更新微信号 - 成功")
    public void testUpdateWechat_Success() throws Exception {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setWechat("new_wechat_id");

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/wechat")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-EDIT-011: 更新个人简介 - 成功")
    public void testUpdateBio_Success() throws Exception {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio("这是我的新简介，热爱技术，享受生活！");

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/bio")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-EDIT-012: 上传头像 - 成功")
    public void testUploadAvatar_Success() throws Exception {
        // Given: 模拟图片文件
        MockMultipartFile avatarFile = new MockMultipartFile(
            "avatar",
            "avatar.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        String expectedUrl = "https://cdn.example.com/avatars/user_" + TEST_USER_ID + ".jpg";
        when(userService.uploadAvatar(eq(TEST_USER_ID), any()))
            .thenReturn(R.ok(expectedUrl));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/user/profile/avatar/upload")
                .file(avatarFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(expectedUrl));

        verify(userService, times(1)).uploadAvatar(eq(TEST_USER_ID), any());
    }

    @Test
    @DisplayName("TC-EDIT-013: 上传头像 - 文件为空")
    public void testUploadAvatar_EmptyFile() throws Exception {
        // Given: 空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
            "avatar",
            "avatar.jpg",
            "image/jpeg",
            new byte[0]
        );

        when(userService.uploadAvatar(eq(TEST_USER_ID), any()))
            .thenReturn(R.fail("文件不能为空"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/user/profile/avatar/upload")
                .file(emptyFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("TC-EDIT-014: 实时保存机制 - 300ms防抖测试")
    public void testRealTimeSave_DebounceTest() throws Exception {
        // 说明: 此测试验证前端实时保存机制的后端支持
        // 前端会在用户输入停止300ms后自动调用更新接口
        // 后端应能正确处理频繁的更新请求

        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio("测试防抖");

        when(userService.updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class)))
            .thenReturn(R.ok());

        // 模拟连续3次更新请求（前端防抖后只发送最后一次）
        for (int i = 0; i < 3; i++) {
            dto.setBio("测试防抖 - 版本" + (i + 1));
            mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/bio")
                    .header(AUTHORIZATION_HEADER, getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());
        }

        // 验证service被调用了3次（实际场景中前端防抖会减少调用次数）
        verify(userService, times(3)).updateUserProfile(eq(TEST_USER_ID), any(UserUpdateDto.class));
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("TC-ERROR-001: 未认证访问 - 应返回401")
    public void testUnauthorizedAccess() throws Exception {
        // When & Then: 不带token访问
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/header")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserProfile(anyLong());
    }

    @Test
    @DisplayName("TC-ERROR-002: 访问不存在的用户 - 应返回404")
    public void testGetNonExistentUser() throws Exception {
        // Given
        Long nonExistentUserId = 99999L;
        when(userService.getOtherUserProfile(TEST_USER_ID, nonExistentUserId))
            .thenReturn(R.fail(404, "用户不存在"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + nonExistentUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404));
    }
}
