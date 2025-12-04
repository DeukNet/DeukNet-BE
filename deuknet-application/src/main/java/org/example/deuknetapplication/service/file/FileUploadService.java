package org.example.deuknetapplication.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.common.exception.InvalidFileException;
import org.example.deuknetapplication.port.in.file.UploadFileUseCase;
import org.example.deuknetapplication.port.out.external.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 업로드 Service
 *
 * 책임:
 * - 파일 크기 비즈니스 규칙 검증
 * - 파일 URL 생성
 * - 파일 업로드 조율
 *
 * 참고:
 * - MIME 타입/확장자 검증은 FileStoragePort 구현체(Adapter)에서 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileUploadService implements UploadFileUseCase {

    private final FileStoragePort fileStoragePort;

    @Value("${app.base-url}")
    private String baseUrl;

    // 최대 파일 크기 (10MB) - 비즈니스 규칙
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    @Transactional
    public FileUploadResponse uploadFile(FileUploadRequest request) {
        log.info("파일 업로드 요청: {}, size: {}", request.originalFileName(), request.size());

        // 파일 크기 검증 (비즈니스 규칙)
        validateFileSize(request.size());

        // 파일 업로드 (MIME/확장자 검증은 Adapter에서 수행)
        String fileName = fileStoragePort.uploadFile(
                request.originalFileName(),
                request.inputStream(),
                request.contentType(),
                request.size()
        );

        // 파일 URL 생성
        String fileUrl = buildFileUrl(fileName);

        log.info("파일 업로드 완료: {}", fileName);

        return new FileUploadResponse(fileName, fileUrl, request.size());
    }

    /**
     * 파일 크기 검증 (비즈니스 규칙)
     */
    private void validateFileSize(long size) {
        if (size == 0) {
            throw new InvalidFileException("파일이 비어있습니다");
        }

        if (size > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB", MAX_FILE_SIZE / 1024 / 1024)
            );
        }
    }

    /**
     * 파일 전체 URL 생성
     */
    private String buildFileUrl(String fileName) {
        return baseUrl + "/api/files/" + fileName;
    }
}
