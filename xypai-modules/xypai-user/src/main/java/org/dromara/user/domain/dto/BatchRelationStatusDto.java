package org.dromara.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 批量查询关系状态请求DTO
 * Batch query relation status request DTO
 *
 * @author XiangYuPai
 * @since 2025-12-03
 */
@Data
@Schema(description = "Batch relation status request")
public class BatchRelationStatusDto {

    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = 100, message = "单次查询最多100个用户")
    @Schema(description = "Target user IDs to check relation status")
    private List<Long> userIds;
}
