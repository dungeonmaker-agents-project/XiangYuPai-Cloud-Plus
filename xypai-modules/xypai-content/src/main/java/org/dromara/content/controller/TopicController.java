package org.dromara.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.web.core.BaseController;
import org.dromara.content.domain.vo.TopicListVO;
import org.dromara.content.service.ITopicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 话题管理控制器
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/content/topics")
@RequiredArgsConstructor
@Tag(name = "话题管理", description = "话题查询和搜索相关接口")
public class TopicController extends BaseController {

    private final ITopicService topicService;

    /**
     * 获取热门话题列表
     */
    @Operation(summary = "获取热门话题列表", description = "获取热门话题,按帖子数和参与人数排序")
    @GetMapping("/hot")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<Page<TopicListVO>> getHotTopics(
        @Parameter(description = "页码,从1开始", example = "1")
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer page,

        @Parameter(description = "每页数量,最大100", example = "20")
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页数量最小为1") @Max(value = 100, message = "每页数量最大为100") Integer pageSize
    ) {
        Page<TopicListVO> result = topicService.getHotTopics(page, pageSize);
        return R.ok(result);
    }

    /**
     * 搜索话题
     */
    @Operation(summary = "搜索话题", description = "根据关键词搜索话题名称和描述")
    @GetMapping("/search")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<Page<TopicListVO>> searchTopics(
        @Parameter(description = "搜索关键词,1-20字符", example = "探店", required = true)
        @RequestParam @NotBlank(message = "搜索关键词不能为空") @Size(min = 1, max = 20, message = "关键词长度为1-20字符") String keyword,

        @Parameter(description = "页码,从1开始", example = "1")
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer page,

        @Parameter(description = "每页数量,最大100", example = "20")
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页数量最小为1") @Max(value = 100, message = "每页数量最大为100") Integer pageSize
    ) {
        Page<TopicListVO> result = topicService.searchTopics(keyword, page, pageSize);
        return R.ok(result);
    }

}
