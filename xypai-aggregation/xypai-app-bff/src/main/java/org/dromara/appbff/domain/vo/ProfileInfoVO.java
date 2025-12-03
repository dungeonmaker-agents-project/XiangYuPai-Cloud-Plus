package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户资料详情VO
 * 对应UI文档中的 ProfileInfoData
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户资料详情")
public class ProfileInfoVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "居住地")
    private String residence;

    @Schema(description = "IP归属地")
    private String ipLocation;

    @Schema(description = "身高(cm)")
    private Integer height;

    @Schema(description = "体重(kg)")
    private Integer weight;

    @Schema(description = "职业")
    private String occupation;

    @Schema(description = "微信号（脱敏或完整）")
    private String wechat;

    @Schema(description = "是否已解锁微信")
    private Boolean wechatUnlocked;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "星座")
    private String zodiac;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "个人简介")
    private String bio;
}
