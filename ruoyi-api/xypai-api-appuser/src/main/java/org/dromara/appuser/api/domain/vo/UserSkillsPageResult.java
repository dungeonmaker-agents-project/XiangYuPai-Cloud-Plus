package org.dromara.appuser.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户技能列表分页结果VO
 *
 * <p>对应UI文档中的 SkillsListData</p>
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillsPageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 技能列表
     */
    private List<UserSkillVo> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 是否有更多
     */
    private Boolean hasMore;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
