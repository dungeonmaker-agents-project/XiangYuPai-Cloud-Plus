package org.dromara.appbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.LimitedTimeQueryDTO;
import org.dromara.appbff.domain.vo.LimitedTimeResultVO;
import org.dromara.appbff.service.HomeLimitedTimeService;
import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 限时专享控制器
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/home/limited-time")
@RequiredArgsConstructor
@Tag(name = "限时专享", description = "限时专享用户列表相关接口")
public class HomeLimitedTimeController {

    private final HomeLimitedTimeService limitedTimeService;

    /**
     * 获取限时专享列表
     */
    @Operation(summary = "获取限时专享列表", description = "获取限时优惠的服务提供者列表，支持筛选和排序")
    @GetMapping("/list")
    public R<LimitedTimeResultVO> getLimitedTimeList(@Valid LimitedTimeQueryDTO queryDTO) {
        log.info("收到限时专享列表请求: {}", queryDTO);

        // 设置默认值
        if (queryDTO.getPageNum() == null) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null) {
            queryDTO.setPageSize(10);
        }
        if (queryDTO.getSortBy() == null || queryDTO.getSortBy().isEmpty()) {
            queryDTO.setSortBy("smart_recommend");
        }
        if (queryDTO.getGender() == null || queryDTO.getGender().isEmpty()) {
            queryDTO.setGender("all");
        }
        if (queryDTO.getLanguage() == null || queryDTO.getLanguage().isEmpty()) {
            queryDTO.setLanguage("all");
        }

        LimitedTimeResultVO result = limitedTimeService.getLimitedTimeList(queryDTO);
        return R.ok(result);
    }
}
