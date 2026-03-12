package com.community.modules.common.controller;

import com.community.common.exception.BusinessException;
import com.community.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_SUFFIX = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final String uploadDir;

    public CommonController(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Operation(summary = "Upload image file")
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestPart("file") MultipartFile file,
                                                        HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请先选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 5MB");
        }
        String originalName = file.getOriginalFilename();
        String suffix = getSuffix(originalName);
        if (!ALLOWED_SUFFIX.contains(suffix.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "仅支持 jpg、jpeg、png、webp、gif 图片格式");
        }

        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            String fileName = UUID.randomUUID() + suffix.toLowerCase(Locale.ROOT);
            Path target = root.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath("/uploads/" + fileName)
                    .replaceQuery(null)
                    .build()
                    .toUriString();
            return ApiResponse.success(Map.of(
                    "url", fileUrl,
                    "name", fileName,
                    "size", file.getSize(),
                    "originalName", originalName
            ));
        } catch (IOException ex) {
            throw new BusinessException(500, "图片上传失败，请稍后重试");
        }
    }

    private String getSuffix(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            throw new BusinessException(400, "文件名无效");
        }
        int index = originalName.lastIndexOf('.');
        if (index < 0) {
            throw new BusinessException(400, "文件格式无效");
        }
        return originalName.substring(index);
    }
}
