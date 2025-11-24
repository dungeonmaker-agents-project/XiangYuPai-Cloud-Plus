package org.dromara.aggregation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.aggregation.domain.dto.FilterApplyDTO;
import org.dromara.aggregation.domain.vo.FilterConfigVO;
import org.dromara.aggregation.domain.vo.FilterResultVO;
import org.dromara.aggregation.service.HomeFilterService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页筛选控制器
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/home/filter")
@Tag(name = "首页筛选", description = "首页筛选功能")
public class HomeFilterController {

    private final HomeFilterService homeFilterService;

    /**
     * 获取筛选配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取筛选配置", description = "获取线上/线下模式的筛选配置选项")
    public R<FilterConfigVO> getFilterConfig(
        @Parameter(description = "类型: online-线上, offline-线下", example = "online")
        @RequestParam(value = "type", defaultValue = "online") String type
    ) {
        log.info("【首页筛选】获取筛选配置, type: {}", type);

        FilterConfigVO config = homeFilterService.getFilterConfig(type);

        log.info("【首页筛选】筛选配置返回成功");
        return R.ok(config);
    }

    /**
     * 应用筛选条件
     */
    @PostMapping("/apply")
    @Operation(summary = "应用筛选条件", description = "应用筛选条件并返回筛选后的用户列表")
    public R<FilterResultVO> applyFilter(
        @Parameter(description = "筛选条件", required = true)
        @Validated @RequestBody FilterApplyDTO dto
    ) {
        log.info("【首页筛选】应用筛选条件, type: {}, pageNum: {}, pageSize: {}",
            dto.getType(), dto.getPageNum(), dto.getPageSize());

        FilterResultVO result = homeFilterService.applyFilter(dto);

        log.info("【首页筛选】筛选结果返回成功, 用户数: {}, 筛选条件数: {}",
            result.getList().size(), result.getAppliedFilters().getCount());
        return R.ok(result);
    }
}
