package com.vblog.controller;

import cn.hutool.core.io.FileUtil;
import com.vblog.common.Result;
import com.vblog.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    // 文件保存的根路径 (动态获取项目根目录)
    private static final String UPLOAD_ROOT_PATH = System.getProperty("user.dir") + "/src/main/resources/vblog_upload/";

    /**
     * 上传图片文件
     */
    @PostMapping("/upload/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return Result.error(40001, "上传文件不能为空");
            }

            // 2. 生成唯一文件名 (例如 uuid.jpg)
            String originalFilename = file.getOriginalFilename();
            String suffix = FileUtil.getSuffix(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + suffix;

            // 3. 创建 images 子目录（如果不存在）
            File imageDir = new File(UPLOAD_ROOT_PATH + "images/");
            if (!imageDir.exists()) {
                imageDir.mkdirs();
                log.info("创建图片目录: {}", imageDir.getAbsolutePath());
            }

            // 4. 保存到物理硬盘
            // 实际路径: Q:/gr_work/.../vblog_upload/images/uuid.jpg
            File dest = new File(imageDir, fileName);
            file.transferTo(dest);

            log.info("图片上传成功: {}", dest.getAbsolutePath());

            // 5. 【关键】返回给前端的是 URL 路径
            // 返回: /api/uploads/images/uuid.jpg (因为 context-path 是 /api)
            String url = "/api/uploads/images/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("图片上传成功", result);

        } catch (IOException e) {
            log.error("Image upload failed: ", e);
            return Result.error(40001, "图片上传失败: " + e.getMessage());
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
            // 1. 验证文件
            if (file.isEmpty()) {
                return Result.error(40001, "上传文件不能为空");
            }

            // 2. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = FileUtil.getSuffix(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + suffix;

            // 3. 创建 videos 子目录（如果不存在）
            File videoDir = new File(UPLOAD_ROOT_PATH + "videos/");
            if (!videoDir.exists()) {
                videoDir.mkdirs();
                log.info("创建视频目录: {}", videoDir.getAbsolutePath());
            }

            // 4. 保存到物理硬盘
            File dest = new File(videoDir, fileName);
            file.transferTo(dest);

            log.info("视频上传成功: {}", dest.getAbsolutePath());

            // 5. 返回 URL 路径
            String url = "/api/uploads/videos/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("视频上传成功", result);

        } catch (IOException e) {
            log.error("Video upload failed: ", e);
            return Result.error(40001, "视频上传失败: " + e.getMessage());
        }
    }
}