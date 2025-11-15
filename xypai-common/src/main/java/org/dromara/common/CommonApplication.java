package org.dromara.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XiangYuPai Common Services Application
 * 享遇派通用服务统一启动类
 *
 * 包含以下业务服务:
 * 1. Location Service - 位置服务 (地点、城市管理)
 * 2. Media Upload Service - 媒体上传服务 (图片、视频处理)
 * 3. Notification Service - 通知服务 (站内信、推送)
 * 4. Report Service - 举报审核服务 (用户举报、内容审核)
 *
 * @author XiangYuPai Team
 */
@SpringBootApplication
public class CommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonApplication.class, args);
        System.out.println("""

            ========================================
            XiangYuPai Common Services Started
            享遇派通用服务已启动
            ========================================
            Services included:
            ✓ Location Service      - 位置服务
            ✓ Media Upload Service  - 媒体上传服务
            ✓ Notification Service  - 通知服务
            ✓ Report Service        - 举报审核服务
            ========================================
            """);
    }
}
