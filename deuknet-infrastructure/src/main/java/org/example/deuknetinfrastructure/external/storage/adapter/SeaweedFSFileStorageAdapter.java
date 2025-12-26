package org.example.deuknetinfrastructure.external.storage.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.common.exception.InvalidFileException;
import org.example.deuknetapplication.port.out.external.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * SeaweedFS 파일 저장소 Adapter
 * <br>
 * 책임:
 * - 파일 저장소 기술적 세부사항 처리
 * - 파일 업로드 전 최종 검증 (MIME, 확장자)
 * - SeaweedFS HTTP API 통신 및 예외 처리
 */
@Slf4j
@Component
public class SeaweedFSFileStorageAdapter implements FileStoragePort {

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

    private final RestTemplate restTemplate;
    private final String masterUrl;
    private final String externalUrl;

    public SeaweedFSFileStorageAdapter(
            RestTemplate restTemplate,
            @Qualifier("seaweedFSMasterUrl") String masterUrl,
            @Qualifier("seaweedFSExternalUrl") String externalUrl
    ) {
        this.restTemplate = restTemplate;
        this.masterUrl = masterUrl;
        this.externalUrl = externalUrl;
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
            // 1. Master에서 업로드 위치 할당 받기
            AssignResponse assignResponse = assignFileId();
            if (assignResponse == null || assignResponse.getFid() == null) {
                throw new RuntimeException("Failed to assign file ID from SeaweedFS");
            }

            String fid = assignResponse.getFid();
            String uploadUrl = "http://" + assignResponse.getPublicUrl() + "/" + fid;

            log.info("SeaweedFS assigned fid: {}, uploadUrl: {}", fid, uploadUrl);

            // 2. 할당받은 위치로 파일 업로드
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new InputStreamResource(inputStream) {
                @Override
                public String getFilename() {
                    return fileName;
                }

                @Override
                public long contentLength() {
                    return size;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Failed to upload file to SeaweedFS: " + response.getStatusCode());
            }

            log.info("File uploaded to SeaweedFS: fid={}, original={}, contentType={}",
                    fid, fileName, contentType);

            // fid를 반환 (파일 식별자)
            return fid;

        } catch (Exception e) {
            log.error("Failed to upload file to SeaweedFS: {}", fileName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * SeaweedFS Master에서 파일 ID 할당받기
     */
    private AssignResponse assignFileId() {
        try {
            String assignUrl = masterUrl + "/dir/assign";
            ResponseEntity<AssignResponse> response = restTemplate.getForEntity(
                    assignUrl,
                    AssignResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }

            throw new RuntimeException("Failed to assign file ID: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to assign file ID from SeaweedFS master", e);
            throw new RuntimeException("Failed to assign file ID", e);
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
    public void deleteFile(String fid) {
        try {
            // SeaweedFS의 파일 위치 조회
            LookupResponse lookupResponse = lookupFileLocation(fid);
            if (lookupResponse == null || lookupResponse.getLocations() == null || lookupResponse.getLocations().isEmpty()) {
                log.warn("File not found for deletion: {}", fid);
                return;
            }

            String volumeUrl = lookupResponse.getLocations().get(0).getPublicUrl();
            String deleteUrl = "http://" + volumeUrl + "/" + fid;

            restTemplate.delete(deleteUrl);
            log.info("File deleted from SeaweedFS: {}", fid);
        } catch (Exception e) {
            log.error("Failed to delete file from SeaweedFS: {}", fid, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    /**
     * SeaweedFS에서 파일 위치 조회
     */
    private LookupResponse lookupFileLocation(String fid) {
        try {
            String lookupUrl = masterUrl + "/dir/lookup?volumeId=" + extractVolumeId(fid);
            ResponseEntity<LookupResponse> response = restTemplate.getForEntity(
                    lookupUrl,
                    LookupResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }

            throw new RuntimeException("Failed to lookup file location: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to lookup file location for fid: {}", fid, e);
            throw new RuntimeException("Failed to lookup file location", e);
        }
    }

    /**
     * fid에서 volumeId 추출
     * 예: "3,01637037d6" -> "3"
     */
    private String extractVolumeId(String fid) {
        if (fid.contains(",")) {
            return fid.split(",")[0];
        }
        return fid;
    }

    @Override
    public String getFileUrl(String fid) {
        // SeaweedFS 외부 접근 URL
        return externalUrl + "/" + fid;
    }

    @Override
    public InputStream downloadFile(String fid) {
        try {
            // 파일 위치 조회
            LookupResponse lookupResponse = lookupFileLocation(fid);
            if (lookupResponse == null || lookupResponse.getLocations() == null || lookupResponse.getLocations().isEmpty()) {
                throw new RuntimeException("File not found: " + fid);
            }

            String volumeUrl = lookupResponse.getLocations().get(0).getPublicUrl();
            String downloadUrl = "http://" + volumeUrl + "/" + fid;

            ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return new java.io.ByteArrayInputStream(response.getBody());
            }

            throw new RuntimeException("Failed to download file");
        } catch (Exception e) {
            log.error("Failed to download file from SeaweedFS: {}", fid, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public String getContentType(String fid) {
        try {
            // 파일 위치 조회
            LookupResponse lookupResponse = lookupFileLocation(fid);
            if (lookupResponse == null || lookupResponse.getLocations() == null || lookupResponse.getLocations().isEmpty()) {
                throw new RuntimeException("File not found: " + fid);
            }

            String volumeUrl = lookupResponse.getLocations().get(0).getPublicUrl();
            String headUrl = "http://" + volumeUrl + "/" + fid;

            ResponseEntity<Void> response = restTemplate.exchange(
                    headUrl,
                    HttpMethod.HEAD,
                    null,
                    Void.class
            );

            HttpHeaders headers = response.getHeaders();
            MediaType contentType = headers.getContentType();
            if (contentType != null) {
                return contentType.toString();
            }

            // Content-Type을 찾을 수 없으면 기본값 반환
            return "application/octet-stream";
        } catch (Exception e) {
            log.error("Failed to get content type from SeaweedFS: {}", fid, e);
            return "application/octet-stream";
        }
    }

    /**
     * SeaweedFS Assign API 응답
     */
    @Data
    private static class AssignResponse {
        private String fid;
        private String url;
        @JsonProperty("publicUrl")
        private String publicUrl;
        private int count;
    }

    /**
     * SeaweedFS Lookup API 응답
     */
    @Data
    private static class LookupResponse {
        private String volumeId;
        private List<Location> locations;

        @Data
        public static class Location {
            private String url;
            @JsonProperty("publicUrl")
            private String publicUrl;
        }
    }
}
