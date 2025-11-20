package org.dromara.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.web.core.BaseController;
import org.dromara.content.domain.dto.ReportDTO;
import org.dromara.content.domain.vo.ReportVO;
import org.dromara.content.service.IReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 举报管理控制器
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "举报管理", description = "内容举报相关接口")
public class ReportController extends BaseController {

    private final IReportService reportService;

    /**
     * 提交举报
     */
    @Operation(summary = "提交举报", description = "举报动态、评论或用户")
    @PostMapping("/report")
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<ReportVO> submitReport(@Valid @RequestBody ReportDTO reportDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        ReportVO result = reportService.submitReport(reportDTO, userId);
        return R.ok("已收到您的举报,我们会尽快处理", result);
    }

}
