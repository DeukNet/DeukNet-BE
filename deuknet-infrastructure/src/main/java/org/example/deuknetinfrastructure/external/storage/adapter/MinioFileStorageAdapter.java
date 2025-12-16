package org.example.deuknetinfrastructure.external.storage.adapter;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.common.exception.InvalidFileException;
import org.example.deuknetapplication.port.out.external.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * MinIO 파일 저장소 Adapter
 * <br>
 * 책임:
 * - 파일 저장소 기술적 세부사항 처리
 * - 파일 업로드 전 최종 검증 (MIME, 확장자)
 * - MinIO 통신 및 예외 처리
 */
@Slf4j
@Component
public class MinioFileStorageAdapter implements FileStoragePort {

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "gif", "png", "jpg", "jpeg", "pdf", "mp3", "webp", "svg",
            "mp4", "mpeg", "avi", "webm", // 비디오
            "wav", "ogg", // 오디오
            "zip", "rar", "7z", // 압축
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md" // 문서
    );

    // 허용된 MIME 타입 (화이트리스트)
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 이미지
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            // 문서
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // 텍스트
            "text/plain", "text/csv", "text/markdown",
            // 압축
            "application/zip", "application/x-zip-compressed",
            "application/x-rar-compressed", "application/x-7z-compressed",
            // 비디오
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm",
            // 오디오
            "audio/mpeg", "audio/wav", "audio/webm", "audio/ogg"
    );

    private final MinioClient minioClient;
    private final String bucketName;
    private final String minioExternalUrl;

    public MinioFileStorageAdapter(
            MinioClient minioClient,
            @Qualifier("minioBucketName") String bucketName,
            @Qualifier("minioExternalUrl") String minioExternalUrl
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.minioExternalUrl = minioExternalUrl;
    }

    @PostConstruct
    public void init() {
        try {
            // 버킷이 없으면 생성
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

                // 버킷을 public으로 설정
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": "*"},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucketName);

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );

                log.info("MinIO bucket created: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }

    @Override
    public String uploadFile(String fileName, InputStream inputStream, String contentType, long size) {
        // 파일명 검증
        validateFileName(fileName);

        // MIME 타입 검증
        validateContentType(contentType, fileName);

        // 확장자 검증
        String extension = getFileExtension(fileName);
        validateExtension(extension, fileName);

        try {
            // 고유한 파일명 생성 (UUID + 확장자)
            String uniqueFileName = UUID.randomUUID() + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("File uploaded to MinIO: {} (original: {}, contentType: {})",
                    uniqueFileName, fileName, contentType);
            return uniqueFileName;

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", fileName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * 파일명 검증 (경로 탐색 공격 방지)
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileException("파일명이 비어있습니다");
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            log.warn("[SECURITY] 경로 탐색 시도 감지: {}", fileName);
            throw new InvalidFileException("유효하지 않은 파일명입니다");
        }
    }

    /**
     * MIME 타입 검증 (악성 파일 업로드 방지)
     */
    private void validateContentType(String contentType, String fileName) {
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidFileException("파일 타입을 확인할 수 없습니다");
        }

        if (!ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("[SECURITY] 허용되지 않은 MIME 타입 업로드 시도: {} (파일: {})",
                    contentType, fileName);
            throw new InvalidFileException("허용되지 않은 파일 타입입니다: " + contentType);
        }
    }

    /**
     * 파일 확장자 검증 (이중 체크)
     */
    private void validateExtension(String extension, String fileName) {
        if (extension.isEmpty()) {
            throw new InvalidFileException("파일 확장자가 없습니다");
        }

        String ext = extension.substring(1).toLowerCase(); // '.' 제거
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            log.warn("[SECURITY] 허용되지 않은 확장자 업로드 시도: {} (파일: {})",
                    extension, fileName);
            throw new InvalidFileException("허용되지 않은 파일 확장자입니다: " + extension);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex <= 0 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex); // '.' 포함
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted from MinIO: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        // MinIO 외부 접근 URL: http://minikube-ip:nodeport/bucket-name/file-name
        return minioExternalUrl + "/" + bucketName + "/" + fileName;
    }

    @Override
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", fileName, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public String getContentType(String fileName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return stat.contentType();
        } catch (Exception e) {
            log.error("Failed to get content type from MinIO: {}", fileName, e);
            // 파일 확장자로 추측
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            return switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "pdf" -> "application/pdf";
                case "mp3" -> "audio/mpeg";
                default -> "application/octet-stream";
            };
        }
    }
}
