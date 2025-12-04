package org.example.deuknetapplication.port.out.external.storage;

import java.io.InputStream;

/**
 * 파일 저장소 Port (out)
 */
public interface FileStoragePort {

    /**
     * 파일 업로드
     *
     * @param fileName 파일 이름
     * @param inputStream 파일 입력 스트림
     * @param contentType 파일 타입
     * @param size 파일 크기
     * @return 업로드된 파일의 URL
     */
    String uploadFile(String fileName, InputStream inputStream, String contentType, long size);

    /**
     * 파일 삭제
     *
     * @param fileName 파일 이름
     */
    void deleteFile(String fileName);

    /**
     * 파일 URL 생성
     *
     * @param fileName 파일 이름
     * @return 파일 접근 URL
     */
    String getFileUrl(String fileName);

    /**
     * 파일 다운로드
     *
     * @param fileName 파일 이름
     * @return 파일 입력 스트림
     */
    InputStream downloadFile(String fileName);

    /**
     * 파일 Content Type 조회
     *
     * @param fileName 파일 이름
     * @return Content Type
     */
    String getContentType(String fileName);
}
