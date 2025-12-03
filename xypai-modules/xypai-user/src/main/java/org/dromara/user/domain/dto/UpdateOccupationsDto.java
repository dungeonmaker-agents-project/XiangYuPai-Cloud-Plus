package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新用户职业DTO（支持多职业）
 * Update User Occupations DTO
 *
 * 对应UI文档: 个人主页-编辑_结构文档.md
 * - 支持多职业标签选择
 * - 每项1-30字符，最多5个
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Data
@Schema(description = "Update user occupations request (multi-occupation support)")
public class UpdateOccupationsDto {

    @Schema(description = "职业列表（最多5个，每项1-30字符）")
    @NotEmpty(message = "职业列表不能为空")
    @Size(max = 5, message = "最多选择5个职业")
    private List<String> occupations;
}
