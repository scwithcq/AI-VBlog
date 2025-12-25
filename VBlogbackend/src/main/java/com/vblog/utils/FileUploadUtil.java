package com.vblog.utils;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传工具类
 * 处理文件上传操作
 * 
 * @author V-Blog团队
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 允许的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    /**
     * 允许的视频类型
     */
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/quicktime", "video/x-msvideo",
            "video/x-ms-wmv", "video/x-flv", "video/webm", "video/avi");

    /**
     * 最大图片文件大小：10MB
     */
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    /**
     * 最大视频文件大小：100MB
     */
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    /**
     * 上传图片文件
     * 
     * @param file MultipartFile对象
     * @return 文件URL路径
     * @throws IOException 如果文件操作失败
     */
    public String uploadImage(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE);
        return uploadFile(file, "images");
    }

    /**
     * 上传视频文件
     * 
     * @param file MultipartFile对象
     * @return 文件URL路径
     * @throws IOException 如果文件操作失败
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE);
        return uploadFile(file, "videos");
    }

    /**
     * 上传文件到指定目录
     * 
     * @param file   MultipartFile对象
     * @param subDir 子目录（images或videos）
     * @return 文件URL路径
     * @throws IOException 如果文件操作失败
     */
    private String uploadFile(MultipartFile file, String subDir) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = IdUtil.simpleUUID() + extension;

        // 如果目录不存在则创建
        Path dirPath = Paths.get(uploadPath, subDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 保存文件
        Path filePath = dirPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        log.info("File uploaded: {}", filePath);
        return "/uploads/" + subDir + "/" + filename;
    }

    /**
     * 验证文件类型和大小
     * 
     * @param file         MultipartFile对象
     * @param allowedTypes 允许的MIME类型
     * @param maxSize      最大文件大小（字节）
     * @throws IOException 如果验证失败
     */
    private void validateFile(MultipartFile file, List<String> allowedTypes, long maxSize) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IOException("Invalid file type: " + contentType);
        }

        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds limit: " + maxSize / 1024 / 1024 + "MB");
        }
    }
}
