package org.example.deuknetapplication.port.in.file;

import java.io.InputStream;

/**
 * 파일 업로드 UseCase (in port)
 */
public interface UploadFileUseCase {

    /**
     * 파일 업로드
     *
     * @param request 파일 업로드 요청
     * @return 파일 업로드 응답
     */
    FileUploadResponse uploadFile(FileUploadRequest request);

    /**
     * 파일 업로드 요청
     */
    record FileUploadRequest(
        String originalFileName,
        InputStream inputStream,
        String contentType,
        long size
    ) {}

    /**
     * 파일 업로드 응답
     */
    record FileUploadResponse(
        String fileName,
        String fileUrl,
        long size
    ) {}
}
