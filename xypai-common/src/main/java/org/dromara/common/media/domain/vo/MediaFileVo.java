package org.dromara.common.media.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.media.domain.entity.MediaFile;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 媒体文件视图对象
 * Media File VO
 *
 * @author XiangYuPai Team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MediaFile.class)
public class MediaFileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 媒体文件ID
     */
    @ExcelProperty(value = "文件ID")
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文件类型
     */
    @ExcelProperty(value = "文件类型")
    private String fileType;

    /**
     * 原始文件名
     */
    @ExcelProperty(value = "原始文件名")
    private String originalName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 文件大小 (字节)
     */
    @ExcelProperty(value = "文件大小")
    private Long fileSize;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 视频时长 (秒)
     */
    private Integer duration;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String bizType;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "上传时间")
    private Date createTime;
}
