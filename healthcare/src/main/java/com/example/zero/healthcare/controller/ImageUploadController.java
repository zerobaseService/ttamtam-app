package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.dto.upload.ImageUploadResponse;
import com.example.zero.healthcare.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Upload", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Operation(summary = "일지 이미지 업로드")
    @PostMapping(value = "/journal-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadJournalImage(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "업로드할 이미지 파일 (jpg/jpeg/png, 최대 10MB)",
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                          schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        String imageUrl = imageUploadService.upload(userId, file);
        return ResponseEntity.ok(ApiResponse.ok(new ImageUploadResponse(imageUrl)));
    }
}
