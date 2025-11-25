package org.dromara.appbff.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.HomeFeedQueryDTO;
import org.dromara.appbff.domain.vo.UserCardVO;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页 Feed 流控制器 (BFF 聚合服务)
 *
 * @author XyPai Team
 * @date 2025-11-20
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/home")
public class HomeFeedController {

    /**
     * 获取首页用户推荐 Feed 流
     *
     * @param queryDTO 查询参数
     * @return 用户推荐列表
     */
    @GetMapping("/feed")
    public R<Map<String, Object>> getHomeFeed(HomeFeedQueryDTO queryDTO) {
        log.info("获取首页Feed流 - type: {}, pageNum: {}, cityCode: {}",
            queryDTO.getType(), queryDTO.getPageNum(), queryDTO.getCityCode());

        // 获取当前登录用户ID
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

        // TODO: 这里应该调用 Service 层进行 RPC 聚合
        // 示例代码：模拟返回数据
        List<UserCardVO> mockList = createMockUserList(queryDTO.getType());

        Map<String, Object> result = new HashMap<>();
        result.put("list", mockList);
        result.put("total", (long) mockList.size());
        result.put("hasMore", false);

        return R.ok(result);
    }

    /**
     * 创建模拟用户列表 (临时代码，实际应该通过 RPC 调用)
     */
    private List<UserCardVO> createMockUserList(String type) {
        List<UserCardVO> list = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            UserCardVO user = new UserCardVO();
            user.setUserId((long) (1000 + i));
            user.setNickname(("online".equals(type) ? "线上" : "线下") + "用户" + i);
            user.setAvatar("https://example.com/avatar" + i + ".jpg");
            user.setGender(i % 2 == 0 ? 1 : 2);
            user.setAge(20 + i);
            user.setCity("深圳");
            user.setDistance(1000.0 * i);
            user.setDistanceText(i + "km");
            user.setSkills(List.of("摄影", "视频剪辑"));
            user.setFeedCount(10 + i);
            user.setFansCount(100 + i * 10);
            user.setIsOnline("online".equals(type));
            user.setIsFollowed(false);
            user.setBio("这是用户" + i + "的个人简介");
            list.add(user);
        }

        return list;
    }

}
