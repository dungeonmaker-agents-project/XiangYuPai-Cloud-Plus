package org.dromara.common.media.controller.app;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.media.domain.bo.MediaUploadBo;
import org.dromara.common.media.domain.vo.MediaUploadResultVo;
import org.dromara.common.media.service.IMediaService;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 媒体服务C端控制器
 * Media Service App Controller
 *
 * @author XiangYuPai Team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController extends BaseController {

    private final IMediaService mediaService;

    /**
     * 上传媒体文件
     *
     * @param uploadBo 上传参数
     * @return 上传结果
     */
    @SaCheckRole("user")
    @PostMapping("/upload")
    public R<MediaUploadResultVo> uploadMedia(@Validated @ModelAttribute MediaUploadBo uploadBo) {
        return mediaService.uploadMedia(uploadBo);
    }

    /**
     * 删除媒体文件
     *
     * @param id 媒体文件ID
     * @return 是否成功
     */
    @SaCheckRole("user")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteMedia(@PathVariable Long id) {
        return mediaService.deleteMedia(id);
    }
}
