package org.dromara.common.report.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.report.domain.bo.ReportReviewBo;
import org.dromara.common.report.service.IReportService;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报服务B端管理控制器
 * Report Service Admin Controller
 *
 * @author XiangYuPai Team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/report")
public class ReportManageController extends BaseController {

    private final IReportService reportService;

    /**
     * 审核举报
     *
     * @param reviewBo 审核参数
     * @return 是否成功
     */
    @SaCheckPermission("report:review")
    @PostMapping("/review")
    public R<Boolean> reviewReport(@Validated @RequestBody ReportReviewBo reviewBo) {
        return reportService.reviewReport(reviewBo);
    }
}
