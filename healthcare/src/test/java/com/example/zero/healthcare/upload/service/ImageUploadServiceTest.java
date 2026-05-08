package com.example.zero.healthcare.upload.service;

import com.example.zero.healthcare.config.UploadProperties;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUploadServiceTest {

    @TempDir
    Path tempDir;

    private ImageUploadService service;

    @BeforeEach
    void setUp() {
        UploadProperties props = new UploadProperties();
        props.setDir(tempDir.toString());
        props.setUrlPrefix("/static/journal-images");
        props.setBaseUrl("http://localhost:8080");
        service = new ImageUploadService(props);
    }

    @Test
    @DisplayName("유효한 이미지를 업로드하면 디스크에 저장되고 URL을 반환한다")
    void upload_validImage_savesToDiskAndReturnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String url = service.upload(1L, file);

        assertThat(url).startsWith("http://localhost:8080/static/journal-images/1/");
        assertThat(url).endsWith(".jpg");
        Path userDir = tempDir.resolve("1");
        assertThat(Files.list(userDir).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 파일을 업로드하면 예외가 발생한다")
    void upload_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.upload(1L, file))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("지원하지 않는 콘텐츠 타입이면 예외가 발생한다")
    void upload_unsupportedContentType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> service.upload(1L, file))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("10MB 초과 파일이면 예외가 발생한다")
    void upload_oversize_throws() {
        byte[] big = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", big);

        assertThatThrownBy(() -> service.upload(1L, file))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("사용자 디렉터리가 없으면 첫 업로드 시 자동으로 생성된다")
    void upload_firstTimeForUser_createsUserDir() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "img".getBytes());

        service.upload(99L, file);

        assertThat(Files.exists(tempDir.resolve("99"))).isTrue();
    }
}
