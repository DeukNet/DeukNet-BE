package org.example.deuknetapplication.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.common.exception.FileNotFoundException;
import org.example.deuknetapplication.common.exception.InvalidFileException;
import org.example.deuknetapplication.port.in.file.DownloadFileUseCase;
import org.example.deuknetapplication.port.out.external.storage.FileStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 다운로드 Service

 * 책임:
 * - 파일 다운로드 요청 처리
 * - 파일 존재 여부 확인
 * - 경로 탐색 공격 방지

 * 참고:
 * - MIME 타입 검증은 업로드 시점에 완료됨
 * - 다운로드는 검증된 파일만 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileDownloadService implements DownloadFileUseCase {

    private final FileStoragePort fileStoragePort;

    @Override
    public FileDownloadResponse downloadFile(String fileName) {
        // 파일명 검증 (경로 탐색 공격 방지)
        validateFileName(fileName);

        try {
            String contentType = fileStoragePort.getContentType(fileName);

            return new FileDownloadResponse(
                    fileStoragePort.downloadFile(fileName),
                    contentType
            );
        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", fileName, e);
            throw new FileNotFoundException(fileName, e);
        }
    }

    /**
     * 파일명 검증
     * - 경로 탐색 공격 방지 (../ 등)
     * - null/empty 체크
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileException("파일명이 비어있습니다");
        }

        // 경로 탐색 공격 방지
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            log.warn("경로 탐색 시도 감지: {}", fileName);
            throw new InvalidFileException("유효하지 않은 파일명입니다: " + fileName);
        }
    }
}
