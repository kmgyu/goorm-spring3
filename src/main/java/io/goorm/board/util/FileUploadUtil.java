package io.goorm.board.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 파일 업로드 유틸리티 클래스
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${app.upload.web-root}")
    private String webUploadRoot;

    @Value("${app.upload.document-root}")
    private String documentUploadRoot;

    // 업로드 타입별 경로
    private static final String PRODUCT_PATH = "products";
    private static final String EXCEL_PATH = "excel";

    // 허용 확장자 설정
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> EXCEL_EXTENSIONS = Set.of("xlsx", "xls");

    /**
     * 파일 업로드 타입
     */
    public enum UploadType {
        PRODUCT_IMAGE("products", IMAGE_EXTENSIONS, true),
        EXCEL_DOCUMENT("excel", EXCEL_EXTENSIONS, false);

        private final String path;
        private final Set<String> allowedExtensions;
        private final boolean isWebAccessible;

        UploadType(String path, Set<String> allowedExtensions, boolean isWebAccessible) {
            this.path = path;
            this.allowedExtensions = allowedExtensions;
            this.isWebAccessible = isWebAccessible;
        }

        public String getPath() {
            return path;
        }

        public Set<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        public boolean isWebAccessible() {
            return isWebAccessible;
        }
    }

    /**
     * 상품 이미지 업로드 (기존 메소드 호환성 유지)
     */
    public String uploadProductImage(MultipartFile file) throws IOException {
        return uploadFile(file, UploadType.PRODUCT_IMAGE).getWebUrl();
    }

    /**
     * 파일 업로드 (통합 메소드)
     */
    public FileUploadResult uploadFile(MultipartFile file, UploadType uploadType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidFileExtension(originalFilename, uploadType)) {
            throw new IllegalArgumentException(
                String.format("%s 파일만 업로드 가능합니다. 허용 확장자: %s",
                    uploadType.name(),
                    String.join(", ", uploadType.getAllowedExtensions())
                )
            );
        }

        // 파일명 생성
        String filename = generateFilename(originalFilename, uploadType);

        // 저장 경로 결정
        String rootPath = uploadType.isWebAccessible() ? webUploadRoot : documentUploadRoot;
        Path uploadPath = Paths.get(rootPath, uploadType.getPath());

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        log.info("{} file uploaded: {}", uploadType.name(), filePath);

        // 결과 반환
        String webUrl = uploadType.isWebAccessible()
            ? "/uploads/" + uploadType.getPath() + "/" + filename
            : null;

        return new FileUploadResult(filename, filePath.toString(), webUrl, uploadType);
    }

    /**
     * 파일 삭제 (웹 URL 기반)
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // URL에서 실제 파일 경로 추출
        String relativePath = fileUrl.replace("/uploads/", "");
        Path filePath = Paths.get(webUploadRoot, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Web file deleted: {}", filePath);
        }
    }

    /**
     * 파일 삭제 (전체 경로 기반)
     */
    public void deleteFileByPath(String fullPath) throws IOException {
        if (fullPath == null || fullPath.isEmpty()) {
            return;
        }

        Path filePath = Paths.get(fullPath);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted: {}", filePath);
        }
    }

    /**
     * 파일 확장자 유효성 검증
     */
    private boolean isValidFileExtension(String filename, UploadType uploadType) {
        String extension = getFileExtension(filename).toLowerCase();
        return uploadType.getAllowedExtensions().contains(extension);
    }

    /**
     * 파일명 생성
     */
    private String generateFilename(String originalFilename, UploadType uploadType) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String prefix = uploadType.name().toLowerCase().replace("_", "-");

        return String.format("%s_%s_%s.%s", prefix, timestamp, uuid, extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 파일 업로드 결과
     */
    public static class FileUploadResult {
        private final String filename;
        private final String fullPath;
        private final String webUrl;
        private final UploadType uploadType;

        public FileUploadResult(String filename, String fullPath, String webUrl, UploadType uploadType) {
            this.filename = filename;
            this.fullPath = fullPath;
            this.webUrl = webUrl;
            this.uploadType = uploadType;
        }

        public String getFilename() {
            return filename;
        }

        public String getFullPath() {
            return fullPath;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public UploadType getUploadType() {
            return uploadType;
        }
    }
}