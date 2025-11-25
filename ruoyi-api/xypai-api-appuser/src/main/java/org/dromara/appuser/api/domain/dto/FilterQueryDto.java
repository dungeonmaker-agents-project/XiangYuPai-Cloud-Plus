package org.dromara.appuser.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 筛选查询DTO (用于RPC传输)
 *
 * @author XyPai Team
 * @date 2025-11-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 基础参数 ==========

    /**
     * 类型: online-线上, offline-线下
     */
    private String type;

    /**
     * 页码 (从1开始)
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    // ========== 筛选条件 ==========

    /**
     * 最小年龄
     */
    private Integer ageMin;

    /**
     * 最大年龄 (null表示不限)
     */
    private Integer ageMax;

    /**
     * 性别: all-全部, male-男, female-女
     */
    private String gender;

    /**
     * 状态: online-在线, active_3d-近三天活跃, active_7d-近七天活跃
     */
    private String status;

    /**
     * 技能/段位列表
     */
    private List<String> skills;

    /**
     * 价格范围列表 (如: "4-9", "10-19", "20+")
     */
    private List<String> priceRanges;

    /**
     * 位置列表 (如: "打野", "中路")
     */
    private List<String> positions;

    /**
     * 标签列表 (如: "荣耀王者", "大神认证")
     */
    private List<String> tags;

    // ========== 位置信息 (用于计算距离) ==========

    /**
     * 查询者纬度
     */
    private Double latitude;

    /**
     * 查询者经度
     */
    private Double longitude;
}
