package org.dromara.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报响应VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "举报响应")
public class ReportVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "举报ID")
    private Long reportId;

    @Schema(description = "审核状态: pending=待审核, processing=审核中, approved=已通过, rejected=已拒绝")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
