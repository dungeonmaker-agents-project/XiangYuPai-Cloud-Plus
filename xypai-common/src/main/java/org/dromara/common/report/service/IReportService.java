package org.dromara.common.report.service;

import org.dromara.common.core.domain.R;
import org.dromara.common.report.domain.bo.ReportReviewBo;
import org.dromara.common.report.domain.bo.ReportSubmitBo;

/**
 * 举报服务接口
 * Report Service Interface
 *
 * @author XiangYuPai Team
 */
public interface IReportService {

    /**
     * 提交举报
     *
     * @param submitBo 举报参数
     * @return 是否成功
     */
    R<Boolean> submitReport(ReportSubmitBo submitBo);

    /**
     * 审核举报
     *
     * @param reviewBo 审核参数
     * @return 是否成功
     */
    R<Boolean> reviewReport(ReportReviewBo reviewBo);

    /**
     * 检查用户是否已被封禁
     *
     * @param userId 用户ID
     * @return 是否封禁中
     */
    boolean isUserBanned(Long userId);

    /**
     * 检查用户是否已被禁言
     *
     * @param userId 用户ID
     * @return 是否禁言中
     */
    boolean isUserMuted(Long userId);
}
