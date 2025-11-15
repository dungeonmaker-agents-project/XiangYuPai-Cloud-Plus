package org.dromara.common.report.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 处罚记录实体
 * Punishment Entity
 *
 * @author XiangYuPai Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("punishment")
public class Punishment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 处罚ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 被处罚用户ID
     */
    private Long userId;

    /**
     * 关联举报ID
     */
    private Long reportId;

    /**
     * 处罚类型: warning/mute/ban
     */
    private String type;

    /**
     * 处罚原因
     */
    private String reason;

    /**
     * 处罚时长 (分钟, null表示永久)
     */
    private Integer duration;

    /**
     * 处罚开始时间
     */
    private Date startTime;

    /**
     * 处罚结束时间
     */
    private Date endTime;

    /**
     * 处罚状态: 0=生效中, 1=已解除, 2=已过期
     */
    private Integer status;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作备注
     */
    private String operatorRemark;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(value = "deleted")
    private Long deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(value = "version")
    private Long version;
}
