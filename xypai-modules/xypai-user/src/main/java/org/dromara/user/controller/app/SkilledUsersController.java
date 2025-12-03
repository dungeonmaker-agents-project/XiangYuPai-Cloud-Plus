package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.dto.SkilledUsersQueryDto;
import org.dromara.user.domain.vo.SkilledUsersResultVo;
import org.dromara.user.service.ISkillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 有技能用户列表控制器
 *
 * <p>发现页面 - 获取有技能用户列表的API</p>
 * <p>只返回有上架技能的用户，支持分页、筛选和排序</p>
 *
 * @author XyPai Team
 * @date 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/user/discovery")
@RequiredArgsConstructor
@Tag(name = "发现页面-有技能用户", description = "获取有技能用户列表的接口")
public class SkilledUsersController {

    private final ISkillService skillService;

    /**
     * 获取有技能的用户列表
     *
     * <p>返回所有有上架技能的用户，支持分页和筛选</p>
     *
     * @param pageNum      页码（从1开始）
     * @param pageSize     每页数量
     * @param gender       性别筛选（all/male/female）
     * @param sortBy       排序方式（smart_recommend/price_asc/price_desc/distance_asc）
     * @param cityCode     城市代码（可选）
     * @param districtCode 区县代码（可选）
     * @param latitude     用户纬度（可选，用于计算距离）
     * @param longitude    用户经度（可选，用于计算距离）
     * @return 有技能用户列表
     */
    @Operation(summary = "获取有技能的用户列表",
        description = "获取所有有上架技能的用户列表，支持分页、性别筛选和排序")
    @GetMapping("/skilled-users")
    public R<SkilledUsersResultVo> getSkilledUsers(
        @Parameter(description = "页码", example = "1")
        @RequestParam(defaultValue = "1") Integer pageNum,

        @Parameter(description = "每页数量", example = "20")
        @RequestParam(defaultValue = "20") Integer pageSize,

        @Parameter(description = "性别筛选: all(不限), male(男), female(女)", example = "all")
        @RequestParam(defaultValue = "all") String gender,

        @Parameter(description = "排序方式: smart_recommend(智能推荐), price_asc(价格从低到高), price_desc(价格从高到低), distance_asc(距离最近)",
            example = "smart_recommend")
        @RequestParam(defaultValue = "smart_recommend") String sortBy,

        @Parameter(description = "城市代码", example = "440100")
        @RequestParam(required = false) String cityCode,

        @Parameter(description = "区县代码", example = "440103")
        @RequestParam(required = false) String districtCode,

        @Parameter(description = "用户纬度")
        @RequestParam(required = false) Double latitude,

        @Parameter(description = "用户经度")
        @RequestParam(required = false) Double longitude
    ) {
        log.info("获取有技能用户列表 - pageNum: {}, pageSize: {}, gender: {}, sortBy: {}",
            pageNum, pageSize, gender, sortBy);

        // 构建查询参数
        SkilledUsersQueryDto queryDto = new SkilledUsersQueryDto();
        queryDto.setPageNum(pageNum);
        queryDto.setPageSize(pageSize);
        queryDto.setGender(gender);
        queryDto.setSortBy(sortBy);
        queryDto.setCityCode(cityCode);
        queryDto.setDistrictCode(districtCode);
        queryDto.setLatitude(latitude);
        queryDto.setLongitude(longitude);

        // 调用服务获取有技能用户列表
        SkilledUsersResultVo result = skillService.getSkilledUsers(queryDto);

        log.info("获取有技能用户列表成功 - total: {}, hasMore: {}",
            result.getTotal(), result.getHasMore());

        return R.ok(result);
    }
}
