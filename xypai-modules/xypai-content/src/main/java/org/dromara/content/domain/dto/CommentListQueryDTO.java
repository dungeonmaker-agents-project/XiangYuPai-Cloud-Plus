package org.dromara.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 获取评论列表请求DTO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "获取评论列表请求")
public class CommentListQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "动态ID", example = "123456", required = true)
    @NotNull(message = "动态ID不能为空")
    private Long feedId;

    @Schema(description = "页码", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "20")
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize;

    @Schema(description = "排序方式: time=时间, hot=热度, like=点赞数", example = "time")
    private String sortType;

}
