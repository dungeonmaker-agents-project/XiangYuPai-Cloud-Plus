package org.dromara.user.controller;

import org.dromara.user.BaseTest;
import org.dromara.user.controller.app.SkillConfigController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SkillConfigController 测试类
 *
 * 测试范围:
 * - 获取技能配置(游戏列表、服务类型)
 * - 上传技能图片
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("技能配置控制器测试 - SkillConfigController Tests")
public class SkillConfigControllerTest extends BaseTest {

    @InjectMocks
    private SkillConfigController skillConfigController;

    // ==================== 获取技能配置测试 ====================

    @Test
    @DisplayName("TC-CONFIG-001: 获取技能配置 - 成功")
    public void testGetSkillConfig_Success() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/skills/config")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.games").isArray())
            .andExpect(jsonPath("$.data.serviceTypes").isArray())
            // 验证游戏列表
            .andExpect(jsonPath("$.data.games[0].gameId").value("wzry"))
            .andExpect(jsonPath("$.data.games[0].gameName").value("王者荣耀"))
            .andExpect(jsonPath("$.data.games[0].ranks").isArray())
            .andExpect(jsonPath("$.data.games[1].gameId").value("hpjy"))
            .andExpect(jsonPath("$.data.games[1].gameName").value("和平精英"))
            .andExpect(jsonPath("$.data.games[2].gameId").value("yxlm"))
            .andExpect(jsonPath("$.data.games[2].gameName").value("英雄联盟"))
            .andExpect(jsonPath("$.data.games[3].gameId").value("ys"))
            .andExpect(jsonPath("$.data.games[3].gameName").value("原神"))
            // 验证服务类型列表
            .andExpect(jsonPath("$.data.serviceTypes[0].typeId").value("cleaning"))
            .andExpect(jsonPath("$.data.serviceTypes[0].typeName").value("家政服务"))
            .andExpect(jsonPath("$.data.serviceTypes[1].typeId").value("repair"))
            .andExpect(jsonPath("$.data.serviceTypes[1].typeName").value("维修服务"))
            .andExpect(jsonPath("$.data.serviceTypes[2].typeId").value("photography"))
            .andExpect(jsonPath("$.data.serviceTypes[2].typeName").value("摄影服务"))
            .andExpect(jsonPath("$.data.serviceTypes[3].typeId").value("tutoring"))
            .andExpect(jsonPath("$.data.serviceTypes[3].typeName").value("教学辅导"))
            .andExpect(jsonPath("$.data.serviceTypes[4].typeId").value("fitness"))
            .andExpect(jsonPath("$.data.serviceTypes[4].typeName").value("健身教练"))
            .andExpect(jsonPath("$.data.serviceTypes[5].typeId").value("beauty"))
            .andExpect(jsonPath("$.data.serviceTypes[5].typeName").value("美容美发"));
    }

    @Test
    @DisplayName("TC-CONFIG-002: 验证王者荣耀段位列表")
    public void testGetSkillConfig_WzryRanks() throws Exception {
        // When & Then: 验证王者荣耀的段位列表
        mockMvc.perform(MockMvcRequestBuilders.get("/api/skills/config")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.games[0].ranks[0]").value("青铜"))
            .andExpect(jsonPath("$.data.games[0].ranks[1]").value("白银"))
            .andExpect(jsonPath("$.data.games[0].ranks[2]").value("黄金"))
            .andExpect(jsonPath("$.data.games[0].ranks[3]").value("铂金"))
            .andExpect(jsonPath("$.data.games[0].ranks[4]").value("钻石"))
            .andExpect(jsonPath("$.data.games[0].ranks[5]").value("星耀"))
            .andExpect(jsonPath("$.data.games[0].ranks[6]").value("王者"));
    }

    @Test
    @DisplayName("TC-CONFIG-003: 验证和平精英段位列表")
    public void testGetSkillConfig_HpjyRanks() throws Exception {
        // When & Then: 验证和平精英的段位列表
        mockMvc.perform(MockMvcRequestBuilders.get("/api/skills/config")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.games[1].ranks[0]").value("青铜"))
            .andExpect(jsonPath("$.data.games[1].ranks[7]").value("无敌战神"));
    }

    // ==================== 上传技能图片测试 ====================

    @Test
    @DisplayName("TC-UPLOAD-001: 上传技能图片 - 成功(JPG)")
    public void testUploadSkillImage_Success_JPG() throws Exception {
        // Given: JPG格式图片
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "skill.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(imageFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isString())
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("https://cdn.example.com/skills/")))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("skill.jpg")));
    }

    @Test
    @DisplayName("TC-UPLOAD-002: 上传技能图片 - 成功(PNG)")
    public void testUploadSkillImage_Success_PNG() throws Exception {
        // Given: PNG格式图片
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "skill.png",
            "image/png",
            "fake image content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(imageFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString(".png")));
    }

    @Test
    @DisplayName("TC-UPLOAD-003: 上传技能图片 - 失败(文件为空)")
    public void testUploadSkillImage_EmptyFile() throws Exception {
        // Given: 空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
            "image",
            "skill.jpg",
            "image/jpeg",
            new byte[0]
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(emptyFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("图片不能为空"));
    }

    @Test
    @DisplayName("TC-UPLOAD-004: 上传技能图片 - 失败(文件过大)")
    public void testUploadSkillImage_FileTooLarge() throws Exception {
        // Given: 超过5MB的文件
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
            "image",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(largeFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("图片大小不能超过5MB"));
    }

    @Test
    @DisplayName("TC-UPLOAD-005: 上传技能图片 - 失败(不支持的格式)")
    public void testUploadSkillImage_UnsupportedFormat() throws Exception {
        // Given: GIF格式(不支持)
        MockMultipartFile gifFile = new MockMultipartFile(
            "image",
            "skill.gif",
            "image/gif",
            "fake gif content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(gifFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("仅支持JPG/PNG格式"));
    }

    @Test
    @DisplayName("TC-UPLOAD-006: 上传技能图片 - 失败(BMP格式)")
    public void testUploadSkillImage_BMPFormat() throws Exception {
        // Given: BMP格式(不支持)
        MockMultipartFile bmpFile = new MockMultipartFile(
            "image",
            "skill.bmp",
            "image/bmp",
            "fake bmp content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(bmpFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("仅支持JPG/PNG格式"));
    }

    @Test
    @DisplayName("TC-UPLOAD-007: 上传技能图片 - 验证URL格式")
    public void testUploadSkillImage_URLFormat() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "my_skill_photo.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        // When & Then: 验证返回的URL包含文件名和时间戳
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(imageFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.startsWith("https://cdn.example.com/skills/")))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("my_skill_photo.jpg")));
    }

    @Test
    @DisplayName("TC-UPLOAD-008: 批量上传技能图片 - 最多9张")
    public void testUploadMultipleSkillImages() throws Exception {
        // 说明: 此测试验证可以连续上传多张图片
        // 前端会限制最多9张,后端需要能处理连续的上传请求

        for (int i = 1; i <= 9; i++) {
            MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "skill_" + i + ".jpg",
                "image/jpeg",
                ("fake image " + i).getBytes()
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                    .file(imageFile)
                    .header(AUTHORIZATION_HEADER, getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("skill_" + i + ".jpg")));
        }
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("TC-BOUNDARY-001: 上传图片 - 正好5MB")
    public void testUploadSkillImage_Exactly5MB() throws Exception {
        // Given: 正好5MB的文件
        byte[] content = new byte[5 * 1024 * 1024]; // 5MB
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "5mb.jpg",
            "image/jpeg",
            content
        );

        // When & Then: 应该成功
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(file)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-BOUNDARY-002: 上传图片 - 超过5MB一个字节")
    public void testUploadSkillImage_5MBPlus1Byte() throws Exception {
        // Given: 5MB + 1字节
        byte[] content = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "5mb_plus.jpg",
            "image/jpeg",
            content
        );

        // When & Then: 应该失败
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(file)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("图片大小不能超过5MB"));
    }
}
