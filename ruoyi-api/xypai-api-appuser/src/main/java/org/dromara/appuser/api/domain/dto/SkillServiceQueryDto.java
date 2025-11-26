package org.dromara.appuser.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 技能服务查询 DTO (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillServiceQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 基础参数 ==========

    /**
     * 技能类型 (必填，如: "王者荣耀", "台球")
     */
    private String skillType;

    /**
     * 页码 (从1开始)
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * Tab类型: glory_king, online, offline, my
     */
    private String tabType;

    /**
     * 排序方式: smart(智能), price_asc(价格最低), rating_desc(评分最高), orders_desc(订单最多)
     */
    private String sortBy;

    // ========== 筛选条件 ==========

    /**
     * 性别筛选: all-不限, male-男, female-女
     */
    private String gender;

    /**
     * 在线状态: online-在线, active_3d-3天内活跃, active_7d-7天内活跃
     */
    private String status;

    /**
     * 游戏大区 (如: "微信区", "QQ区")
     */
    private String gameArea;

    /**
     * 段位列表 (如: ["荣耀王者", "超凡大师"])
     */
    private List<String> ranks;

    /**
     * 价格区间列表 (如: ["0-10", "10-30", "50+"])
     */
    private List<String> priceRanges;

    /**
     * 位置/英雄列表 (如: ["打野", "中路"])
     */
    private List<String> positions;

    /**
     * 标签列表 (如: ["可线上", "技术好"])
     */
    private List<String> tags;

    /**
     * 城市代码 (用于线下服务筛选)
     */
    private String cityCode;

    // ========== 位置信息 (用于计算距离) ==========

    /**
     * 当前用户ID (用于个性化推荐)
     */
    private Long currentUserId;

    /**
     * 当前用户经度
     */
    private Double longitude;

    /**
     * 当前用户纬度
     */
    private Double latitude;
}
