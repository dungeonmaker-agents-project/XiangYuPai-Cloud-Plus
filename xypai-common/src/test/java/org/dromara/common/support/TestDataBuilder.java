package org.dromara.common.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Random;

/**
 * 测试数据构建器
 * <p>
 * 提供各种测试数据的创建方法:
 * - 测试图片文件
 * - 测试视频文件
 * - 测试通知数据
 * - 测试城市数据
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataBuilder {

    private final Random random = new Random();

    // ==================== 文件创建相关方法 ====================

    /**
     * 创建测试图片文件
     *
     * @param fileName 文件名
     * @param fileSize 文件大小(字节)
     * @return 图片文件
     */
    public File createTestImageFile(String fileName, long fileSize) {
        return createTestImageFile(fileName, fileSize, 1920, 1080);
    }

    /**
     * 创建测试图片文件 (指定尺寸)
     *
     * @param fileName 文件名
     * @param fileSize 文件大小(字节)
     * @param width    图片宽度
     * @param height   图片高度
     * @return 图片文件
     */
    public File createTestImageFile(String fileName, long fileSize, int width, int height) {
        try {
            // 创建临时目录
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "test-uploads");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // 创建图片
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 填充随机颜色背景
            Color bgColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            g.setColor(bgColor);
            g.fillRect(0, 0, width, height);

            // 添加一些随机形状
            g.setColor(Color.WHITE);
            for (int i = 0; i < 10; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                int size = random.nextInt(100) + 50;
                g.fillOval(x, y, size, size);
            }

            g.dispose();

            // 保存文件
            File file = new File(tempDir, fileName);
            ImageIO.write(image, "jpg", file);

            log.debug("创建测试图片: {}, 实际大小: {} bytes", file.getAbsolutePath(), file.length());

            return file;
        } catch (IOException e) {
            throw new RuntimeException("创建测试图片失败", e);
        }
    }

    /**
     * 创建测试视频文件
     * <p>
     * 注意: 这里创建的是模拟文件，不是真实的视频文件
     *
     * @param fileName 文件名
     * @param fileSize 文件大小(字节)
     * @return 视频文件
     */
    public File createTestVideoFile(String fileName, long fileSize) {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "test-uploads");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            File file = new File(tempDir, fileName);

            // 创建指定大小的模拟视频文件
            byte[] data = new byte[(int) fileSize];
            random.nextBytes(data);
            Files.write(file.toPath(), data);

            log.debug("创建测试视频: {}, 大小: {} bytes", file.getAbsolutePath(), file.length());

            return file;
        } catch (IOException e) {
            throw new RuntimeException("创建测试视频失败", e);
        }
    }

    /**
     * 创建测试文件 (任意类型)
     *
     * @param fileName 文件名
     * @param fileSize 文件大小(字节)
     * @return 文件
     */
    public File createTestFile(String fileName, long fileSize) {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "test-uploads");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            File file = new File(tempDir, fileName);

            // 创建指定大小的文件
            byte[] data = new byte[(int) fileSize];
            random.nextBytes(data);
            Files.write(file.toPath(), data);

            log.debug("创建测试文件: {}, 大小: {} bytes", file.getAbsolutePath(), file.length());

            return file;
        } catch (IOException e) {
            throw new RuntimeException("创建测试文件失败", e);
        }
    }

    // ==================== 测试数据相关方法 ====================

    /**
     * 生成随机经度 (中国范围)
     *
     * @return 经度
     */
    public BigDecimal randomLongitude() {
        // 中国经度范围: 73°E - 135°E
        double lng = 73.0 + random.nextDouble() * 62.0;
        return BigDecimal.valueOf(lng).setScale(6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成随机纬度 (中国范围)
     *
     * @return 纬度
     */
    public BigDecimal randomLatitude() {
        // 中国纬度范围: 18°N - 54°N
        double lat = 18.0 + random.nextDouble() * 36.0;
        return BigDecimal.valueOf(lat).setScale(6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成北京坐标
     *
     * @return [纬度, 经度]
     */
    public BigDecimal[] beijingCoordinates() {
        return new BigDecimal[]{
            BigDecimal.valueOf(39.904989),  // 纬度
            BigDecimal.valueOf(116.405285)  // 经度
        };
    }

    /**
     * 生成上海坐标
     *
     * @return [纬度, 经度]
     */
    public BigDecimal[] shanghaiCoordinates() {
        return new BigDecimal[]{
            BigDecimal.valueOf(31.230416),  // 纬度
            BigDecimal.valueOf(121.473701)  // 经度
        };
    }

    /**
     * 生成深圳坐标
     *
     * @return [纬度, 经度]
     */
    public BigDecimal[] shenzhenCoordinates() {
        return new BigDecimal[]{
            BigDecimal.valueOf(22.543099),  // 纬度
            BigDecimal.valueOf(114.057868)  // 经度
        };
    }

    /**
     * 生成随机手机号
     *
     * @return 手机号
     */
    public String randomMobile() {
        return "138" + String.format("%08d", random.nextInt(100000000));
    }

    /**
     * 生成随机昵称
     *
     * @return 昵称
     */
    public String randomNickname() {
        String[] prefixes = {"测试", "体验", "演示", "示例"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        return prefix + "用户" + random.nextInt(10000);
    }

    /**
     * 生成随机邮箱
     *
     * @return 邮箱
     */
    public String randomEmail() {
        return "test" + random.nextInt(10000) + "@example.com";
    }

    // ==================== 清理方法 ====================

    /**
     * 清理所有测试文件
     */
    public void cleanupTestFiles() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test-uploads");
        if (tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        log.warn("无法删除测试文件: {}", file.getAbsolutePath());
                    }
                }
            }
            if (!tempDir.delete()) {
                log.warn("无法删除测试目录: {}", tempDir.getAbsolutePath());
            }
        }
    }
}
