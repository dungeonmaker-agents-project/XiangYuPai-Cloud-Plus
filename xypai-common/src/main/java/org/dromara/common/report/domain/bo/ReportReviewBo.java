package org.dromara.common.report.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 举报审核业务对象
 * Report Review BO
 *
 * @author XiangYuPai Team
 */
@Data
public class ReportReviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 举报ID
     */
    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    /**
     * 审核结果: 0=无效举报, 1=警告, 2=删除内容, 3=封禁用户
     */
    @NotNull(message = "审核结果不能为空")
    private Integer reviewResult;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 处罚时长 (分钟, 封禁用户时需要)
     */
    private Integer punishmentDuration;
}
