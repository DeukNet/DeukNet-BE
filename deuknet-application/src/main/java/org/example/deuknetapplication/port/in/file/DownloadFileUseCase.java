package org.example.deuknetapplication.port.in.file;

import java.io.InputStream;

/**
 * 파일 다운로드 UseCase (in port)
 */
public interface DownloadFileUseCase {

    /**
     * 파일 다운로드
     *
     * @param fileName 파일명
     * @return 파일 다운로드 응답
     */
    FileDownloadResponse downloadFile(String fileName);

    /**
     * 파일 다운로드 응답
     */
    record FileDownloadResponse(
        InputStream inputStream,
        String contentType
    ) {}
}
