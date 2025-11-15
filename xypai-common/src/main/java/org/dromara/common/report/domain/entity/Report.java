package org.dromara.common.report.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 举报记录实体
 * Report Entity
 *
 * @author XiangYuPai Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report")
public class Report extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 举报ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 举报用户ID
     */
    private Long reporterId;

    /**
     * 被举报用户ID
     */
    private Long reportedUserId;

    /**
     * 被举报内容类型: post/moment/comment/user
     */
    private String contentType;

    /**
     * 被举报内容ID
     */
    private Long contentId;

    /**
     * 举报原因类型: spam/porn/violence/fraud/other
     */
    private String reasonType;

    /**
     * 举报详细说明
     */
    private String reasonDetail;

    /**
     * 证据截图URLs (JSON数组)
     */
    private String evidenceUrls;

    /**
     * 审核状态: 0=待审核, 1=审核中, 2=已处理, 3=已驳回
     */
    private Integer status;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核结果: 0=无效举报, 1=警告, 2=删除内容, 3=封禁用户
     */
    private Integer reviewResult;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 审核时间
     */
    private Date reviewedAt;

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
