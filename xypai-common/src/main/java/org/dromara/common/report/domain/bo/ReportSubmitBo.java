package org.dromara.common.report.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.report.domain.entity.Report;

import java.io.Serial;
import java.io.Serializable;

/**
 * 举报提交业务对象
 * Report Submit BO
 *
 * @author XiangYuPai Team
 */
@Data
@AutoMapper(target = Report.class, reverseConvertGenerate = false)
public class ReportSubmitBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 被举报用户ID
     */
    @NotNull(message = "被举报用户ID不能为空")
    private Long reportedUserId;

    /**
     * 被举报内容类型
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /**
     * 被举报内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 举报原因类型
     */
    @NotBlank(message = "举报原因不能为空")
    private String reasonType;

    /**
     * 举报详细说明 (可选)
     */
    private String reasonDetail;

    /**
     * 证据截图URLs (JSON数组)
     */
    private String evidenceUrls;
}
