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
import java.util.Map;

/**
 * 文件上传工具类
 * 处理文件上传操作
 *
 * @author V-Blog团队
 */
@Slf4j
@Component
public class FileUploadUtil {

    private final com.vblog.config.UploadStorage uploadStorage;

    public FileUploadUtil(com.vblog.config.UploadStorage uploadStorage) {
        this.uploadStorage = uploadStorage;
    }

    /**
     * 允许的图片类型
     */
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg", "image/jpg", ".jpg", "image/png", ".png",
            "image/gif", ".gif", "image/webp", ".webp");

    /**
     * 允许的视频类型
     */
    private static final Map<String, String> ALLOWED_VIDEO_TYPES = Map.of(
            "video/mp4", ".mp4", "video/quicktime", ".mov", "video/x-msvideo", ".avi",
            "video/x-ms-wmv", ".wmv", "video/x-flv", ".flv", "video/webm", ".webm",
            "video/avi", ".avi");

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
        String extension = validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE);
        return uploadFile(file, "images", extension);
    }

    /**
     * 上传视频文件
     *
     * @param file MultipartFile对象
     * @return 文件URL路径
     * @throws IOException 如果文件操作失败
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        String extension = validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE);
        return uploadFile(file, "videos", extension);
    }

    /**
     * 上传文件到指定目录
     *
     * @param file   MultipartFile对象
     * @param subDir 子目录（images或videos）
     * @return 文件URL路径
     * @throws IOException 如果文件操作失败
     */
    private String uploadFile(MultipartFile file, String subDir, String extension) throws IOException {
        String filename = IdUtil.simpleUUID() + extension;

        // 如果目录不存在则创建
        Path dirPath = uploadStorage.root().resolve(subDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 保存文件
        Path filePath = dirPath.resolve(filename);
        try (var input = file.getInputStream()) {
            Files.copy(input, filePath);
        } catch (IOException ex) {
            Files.deleteIfExists(filePath);
            throw ex;
        }

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
    private String validateFile(MultipartFile file, Map<String, String> allowedTypes, long maxSize) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.containsKey(contentType)) {
            throw new IOException("Invalid file type: " + contentType);
        }

        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds limit: " + maxSize / 1024 / 1024 + "MB");
        }
        byte[] header;
        try (var input = file.getInputStream()) { header = input.readNBytes(32); }
        String ext = allowedTypes.get(contentType);
        String ascii = new String(header, java.nio.charset.StandardCharsets.ISO_8859_1);
        // Browsers infer MIME from the filename; accept renamed, supported images and normalize storage.
        if (allowedTypes == ALLOWED_IMAGE_TYPES) {
            if (header.length >= 3 && (header[0] & 255) == 255 && (header[1] & 255) == 216 && (header[2] & 255) == 255) ext = ".jpg";
            else if (header.length >= 8 && java.util.Arrays.equals(java.util.Arrays.copyOf(header, 8), new byte[]{(byte)137,80,78,71,13,10,26,10})) ext = ".png";
            else if (ascii.startsWith("GIF87a") || ascii.startsWith("GIF89a")) ext = ".gif";
            else if (header.length >= 12 && ascii.startsWith("RIFF") && ascii.substring(8).startsWith("WEBP")) ext = ".webp";
            else throw new IOException("Unsupported image signature");
        }
        boolean valid = switch (ext) {
            case ".jpg" -> header.length >= 3 && (header[0] & 255) == 255 && (header[1] & 255) == 216 && (header[2] & 255) == 255;
            case ".png" -> header.length >= 8 && java.util.Arrays.equals(java.util.Arrays.copyOf(header, 8), new byte[]{(byte)137,80,78,71,13,10,26,10});
            case ".gif" -> ascii.startsWith("GIF87a") || ascii.startsWith("GIF89a");
            case ".webp" -> header.length >= 12 && ascii.startsWith("RIFF") && ascii.substring(8).startsWith("WEBP");
            case ".mp4", ".mov" -> header.length >= 12 && ascii.substring(4).startsWith("ftyp");
            case ".webm" -> header.length >= 4 && (header[0] & 255) == 26 && (header[1] & 255) == 69 && (header[2] & 255) == 223 && (header[3] & 255) == 163;
            case ".avi" -> header.length >= 12 && ascii.startsWith("RIFF") && ascii.substring(8).startsWith("AVI ");
            case ".flv" -> ascii.startsWith("FLV");
            case ".wmv" -> header.length >= 4 && (header[0] & 255) == 48 && (header[1] & 255) == 38 && (header[2] & 255) == 178 && (header[3] & 255) == 117;
            default -> false;
        };
        if (!valid) throw new IOException("File signature does not match its media type");
        if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".gif")) {
            try (var input = javax.imageio.ImageIO.createImageInputStream(file.getInputStream())) {
                var readers = javax.imageio.ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IOException("Invalid image");
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    long width = reader.getWidth(0), height = reader.getHeight(0);
                    if (width <= 0 || height <= 0 || width * height > 40000000) throw new IOException("Image dimensions exceed limit");
                } finally { reader.dispose(); }
            }
        }
        return ext;
    }
}
