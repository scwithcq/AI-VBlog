package com.vblog.controller;

import com.vblog.common.Result;
import com.vblog.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author V-Blog团队
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController extends BaseController {

    private final FileUploadUtil fileUploadUtil;

    /**
     * 上传图片文件
     */
    @PostMapping("/upload/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            String url = "/api" + fileUploadUtil.uploadImage(file);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("图片上传成功", result);

        } catch (IOException e) {
            log.error("Image upload failed: ", e);
            return Result.error(40001, "图片无效或无法保存，请检查类型与大小");
        }
    }

    /**
     * 上传视频文件
     */
    @PostMapping("/upload/video")
    public Result<Map<String, String>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            String url = "/api" + fileUploadUtil.uploadVideo(file);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("视频上传成功", result);

        } catch (IOException e) {
            log.error("Video upload failed: ", e);
            return Result.error(40001, "视频无效或无法保存，请检查类型与大小");
        }
    }
}
