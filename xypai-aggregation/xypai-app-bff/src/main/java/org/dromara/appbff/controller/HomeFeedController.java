package org.dromara.appbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.HomeFeedResultVO;
import org.dromara.appbff.service.HomeFeedService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页 Feed 流控制器 (BFF 聚合服务)
 *
 * <p>功能：获取首页用户推荐列表</p>
 * <p>数据来源：通过 RPC 调用 xypai-user 服务获取真实用户数据</p>
 *
 * @author XyPai Team
 * @date 2025-11-20
 * @updated 2025-11-29 改造为 RPC 真实数据调用
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/home")
@Tag(name = "首页用户推荐", description = "首页用户推荐流相关接口")
public class HomeFeedController {

    private final HomeFeedService homeFeedService;

    /**
     * 获取首页用户推荐 Feed 流
     *
     * @param queryDTO 查询参数
     * @return 用户推荐列表
     */
    @Operation(summary = "获取首页用户推荐列表", description = "获取首页用户推荐流，支持分页和类型筛选")
    @GetMapping("/feed")
    public R<HomeFeedResultVO> getHomeFeed(HomeFeedQueryDTO queryDTO) {
        log.info("收到首页Feed流请求 - type: {}, pageNum: {}, pageSize: {}, cityCode: {}",
            queryDTO.getType(), queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getCityCode());

        // 设置默认值
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        if (queryDTO.getType() == null || queryDTO.getType().isEmpty()) {
            queryDTO.setType("offline"); // 默认线下用户
        }

        // 调用 Service 获取真实数据
        HomeFeedResultVO result = homeFeedService.getHomeFeedList(queryDTO);

        log.info("首页Feed流响应 - 返回 {} 条数据, hasMore: {}",
            result.getList() != null ? result.getList().size() : 0,
            result.getHasMore());

        return R.ok(result);
    }
}
