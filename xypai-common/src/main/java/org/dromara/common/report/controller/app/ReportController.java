package org.dromara.common.report.controller.app;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.report.domain.bo.ReportSubmitBo;
import org.dromara.common.report.service.IReportService;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报服务C端控制器
 * Report Service App Controller
 *
 * @author XiangYuPai Team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/report")
public class ReportController extends BaseController {

    private final IReportService reportService;

    /**
     * 提交举报
     *
     * @param submitBo 举报参数
     * @return 是否成功
     */
    @SaCheckRole("user")
    @PostMapping("/submit")
    public R<Boolean> submitReport(@Validated @RequestBody ReportSubmitBo submitBo) {
        return reportService.submitReport(submitBo);
    }
}
