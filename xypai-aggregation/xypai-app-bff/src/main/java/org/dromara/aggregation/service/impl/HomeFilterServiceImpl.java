package org.dromara.aggregation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dromara.aggregation.domain.dto.FilterApplyDTO;
import org.dromara.aggregation.domain.vo.FilterConfigVO;
import org.dromara.aggregation.domain.vo.FilterResultVO;
import org.dromara.aggregation.domain.vo.UserCardVO;
import org.dromara.aggregation.service.HomeFilterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 首页筛选服务实现
 * <p>
 * 当前实现: Mock 数据
 * TODO: 实现真实的 RPC 调用聚合逻辑
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@Service
public class HomeFilterServiceImpl implements HomeFilterService {

    @Override
    public FilterConfigVO getFilterConfig(String type) {
        log.info("获取筛选配置, type: {}", type);

        boolean isOnline = "online".equalsIgnoreCase(type);

        // 构建筛选配置 (Mock 数据)
        FilterConfigVO config = FilterConfigVO.builder()
            .ageRange(FilterConfigVO.AgeRange.builder()
                .min(18)
                .max(null) // null 表示不限
                .build())
            .genderOptions(Arrays.asList(
                FilterConfigVO.Option.builder().value("all").label("全部").build(),
                FilterConfigVO.Option.builder().value("male").label("男").build(),
                FilterConfigVO.Option.builder().value("female").label("女").build()
            ))
            .statusOptions(Arrays.asList(
                FilterConfigVO.Option.builder().value("online").label("在线").build(),
                FilterConfigVO.Option.builder().value("active_3d").label("近三天活跃").build(),
                FilterConfigVO.Option.builder().value("active_7d").label("近七天活跃").build()
            ))
            .tagOptions(Arrays.asList(
                FilterConfigVO.TagOption.builder().value("荣耀王者").label("荣耀王者").highlighted(true).build(),
                FilterConfigVO.TagOption.builder().value("大神认证").label("大神认证").highlighted(true).build(),
                FilterConfigVO.TagOption.builder().value("巅峰赛").label("巅峰赛").highlighted(false).build(),
                FilterConfigVO.TagOption.builder().value("带粉上分").label("带粉上分").highlighted(false).build(),
                FilterConfigVO.TagOption.builder().value("官方认证").label("官方认证").highlighted(true).build(),
                FilterConfigVO.TagOption.builder().value("声优陪玩").label("声优陪玩").highlighted(false).build()
            ))
            .build();

        // 线上模式独有的选项
        if (isOnline) {
            config.setSkillOptions(Arrays.asList(
                FilterConfigVO.SkillOption.builder().value("尊贵钻金").label("尊贵钻金").category("王者荣耀").build(),
                FilterConfigVO.SkillOption.builder().value("永恒钻石").label("永恒钻石").category("王者荣耀").build(),
                FilterConfigVO.SkillOption.builder().value("至尊星耀").label("至尊星耀").category("王者荣耀").build(),
                FilterConfigVO.SkillOption.builder().value("最强王者").label("最强王者").category("王者荣耀").build(),
                FilterConfigVO.SkillOption.builder().value("荣耀王者").label("荣耀王者").category("王者荣耀").build(),
                FilterConfigVO.SkillOption.builder().value("英雄联盟").label("英雄联盟").category("其他游戏").build()
            ));

            config.setPriceOptions(Arrays.asList(
                FilterConfigVO.PriceOption.builder().value("4-9").label("4~9币").min(4).max(9).build(),
                FilterConfigVO.PriceOption.builder().value("10-19").label("10~19币").min(10).max(19).build(),
                FilterConfigVO.PriceOption.builder().value("20+").label("20币以上").min(20).max(null).build()
            ));

            config.setPositionOptions(Arrays.asList(
                FilterConfigVO.Option.builder().value("打野").label("打野").build(),
                FilterConfigVO.Option.builder().value("上路").label("上路").build(),
                FilterConfigVO.Option.builder().value("中路").label("中路").build(),
                FilterConfigVO.Option.builder().value("下路").label("下路").build(),
                FilterConfigVO.Option.builder().value("辅助").label("辅助").build()
            ));
        } else {
            // 线下模式：不设置价格和位置选项
            config.setPriceOptions(null);
            config.setPositionOptions(null);

            // 线下模式的技能选项
            config.setSkillOptions(Arrays.asList(
                FilterConfigVO.SkillOption.builder().value("健身教练").label("健身教练").category("运动健身").build(),
                FilterConfigVO.SkillOption.builder().value("瑜伽老师").label("瑜伽老师").category("运动健身").build(),
                FilterConfigVO.SkillOption.builder().value("摄影师").label("摄影师").category("摄影服务").build(),
                FilterConfigVO.SkillOption.builder().value("化妆师").label("化妆师").category("美妆服务").build()
            ));
        }

        log.info("筛选配置返回成功, 技能数: {}", config.getSkillOptions().size());
        return config;
    }

    @Override
    public FilterResultVO applyFilter(FilterApplyDTO dto) {
        log.info("应用筛选条件, type: {}, pageNum: {}, pageSize: {}",
            dto.getType(), dto.getPageNum(), dto.getPageSize());

        // 统计筛选条件数量
        int filterCount = 0;
        List<String> summary = new ArrayList<>();

        FilterApplyDTO.FilterCriteria filters = dto.getFilters();
        if (filters != null) {
            // 年龄
            if (filters.getAge() != null) {
                filterCount++;
                String ageDesc = filters.getAge().getMin() + "岁";
                if (filters.getAge().getMax() != null) {
                    ageDesc += " - " + filters.getAge().getMax() + "岁";
                } else {
                    ageDesc += " - 不限";
                }
                summary.add("年龄: " + ageDesc);
            }

            // 性别
            if (filters.getGender() != null && !"all".equals(filters.getGender())) {
                filterCount++;
                String genderLabel = "male".equals(filters.getGender()) ? "男" : "女";
                summary.add("性别: " + genderLabel);
            }

            // 状态
            if (filters.getStatus() != null) {
                filterCount++;
                String statusLabel = switch (filters.getStatus()) {
                    case "online" -> "在线";
                    case "active_3d" -> "近三天活跃";
                    case "active_7d" -> "近七天活跃";
                    default -> filters.getStatus();
                };
                summary.add("状态: " + statusLabel);
            }

            // 技能
            if (filters.getSkills() != null && !filters.getSkills().isEmpty()) {
                filterCount++;
                summary.add("技能: " + String.join(", ", filters.getSkills()));
            }

            // 价格
            if (filters.getPriceRange() != null && !filters.getPriceRange().isEmpty()) {
                filterCount++;
                summary.add("价格: " + String.join(", ", filters.getPriceRange()));
            }

            // 位置
            if (filters.getPositions() != null && !filters.getPositions().isEmpty()) {
                filterCount++;
                summary.add("位置: " + String.join(", ", filters.getPositions()));
            }

            // 标签
            if (filters.getTags() != null && !filters.getTags().isEmpty()) {
                filterCount++;
                summary.add("标签: " + String.join(", ", filters.getTags()));
            }
        }

        // 构建 Mock 用户列表
        List<UserCardVO> users = buildMockUserList(dto.getType(), dto.getPageSize());

        // 构建结果
        FilterResultVO result = FilterResultVO.builder()
            .total((long) users.size())
            .hasMore(false)
            .list(users)
            .appliedFilters(FilterResultVO.AppliedFilters.builder()
                .count(filterCount)
                .summary(summary)
                .build())
            .build();

        log.info("筛选结果返回成功, 用户数: {}, 筛选条件数: {}", users.size(), filterCount);
        return result;
    }

    /**
     * 构建 Mock 用户列表
     */
    private List<UserCardVO> buildMockUserList(String type, int pageSize) {
        List<UserCardVO> users = new ArrayList<>();
        boolean isOnline = "online".equalsIgnoreCase(type);

        for (int i = 1; i <= Math.min(pageSize, 5); i++) {
            UserCardVO user = new UserCardVO();
            user.setUserId((long) (1000 + i));
            user.setAvatar("https://example.com/avatar" + i + ".jpg");
            user.setNickname(isOnline ? "线上陪玩" + i : "线下教练" + i);
            user.setAge(20 + i);
            user.setGender(i % 2 == 0 ? 1 : 2); // 1-男, 2-女
            user.setIsOnline(i <= 2); // 前两个在线

            if (isOnline) {
                // 线上：游戏技能
                user.setSkills(Arrays.asList("王者荣耀-荣耀王者", "英雄联盟-钻石"));
                user.setBio("擅长打野，带你上分！价格: " + (10 + i * 5) + "金币/小时");
            } else {
                // 线下：本地服务技能
                user.setSkills(Arrays.asList("台球陪练", "桌游主持"));
                user.setDistance((double) (i * 500)); // 距离: 500m, 1km, 1.5km...
                user.setDistanceText(formatDistance(i * 500));
                user.setCity("深圳");
                user.setBio("专业台球教练，10年经验。价格: " + (50 + i * 20) + "金币/小时");
            }

            user.setFansCount(100 + i * 10);
            user.setFeedCount(50 + i * 5);
            user.setIsFollowed(false);

            users.add(user);
        }

        return users;
    }

    /**
     * 格式化距离文本
     */
    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "m";
        } else {
            return String.format("%.1fkm", meters / 1000.0);
        }
    }
}
