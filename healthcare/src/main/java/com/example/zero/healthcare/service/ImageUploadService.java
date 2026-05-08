package com.example.zero.healthcare.service;

import com.example.zero.healthcare.config.UploadProperties;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png"
    );

    private final UploadProperties uploadProperties;

    public String upload(Long userId, MultipartFile file) {
        validate(file);

        String ext = ALLOWED_TYPES.get(file.getContentType());
        String filename = UUID.randomUUID() + "." + ext;
        Path userDir = Paths.get(uploadProperties.getDir()).resolve(userId.toString());

        try {
            Files.createDirectories(userDir);
            Path target = userDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("uploaded image userId={} bytes={} path={}", userId, file.getSize(), target);
        } catch (IOException e) {
            log.warn("image upload failed userId={}", userId, e);
            throw new CoreException(ErrorCode.INTERNAL_ERROR);
        }

        return uploadProperties.getBaseUrl()
                + uploadProperties.getUrlPrefix()
                + "/" + userId + "/" + filename;
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CoreException(ErrorCode.INVALID_REQUEST);
        }
        if (!ALLOWED_TYPES.containsKey(file.getContentType())) {
            throw new CoreException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
        if (file.getSize() > MAX_BYTES) {
            throw new CoreException(ErrorCode.FILE_TOO_LARGE);
        }
    }
}
