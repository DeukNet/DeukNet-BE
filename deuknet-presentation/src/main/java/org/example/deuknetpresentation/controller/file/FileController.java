package org.example.deuknetpresentation.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetapplication.port.in.file.DownloadFileUseCase;
import org.example.deuknetapplication.port.in.file.UploadFileUseCase;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 파일 업로드 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;

    /**
     * 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        UploadFileUseCase.FileUploadRequest request = new UploadFileUseCase.FileUploadRequest(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getContentType(),
                file.getSize()
        );

        UploadFileUseCase.FileUploadResponse response = uploadFileUseCase.uploadFile(request);

        return ResponseEntity.ok(new FileUploadResponseDto(
                response.fileName(),
                response.fileUrl(),
                response.size()
        ));
    }

    /**
     * 파일 다운로드 (캐싱 지원)
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        DownloadFileUseCase.FileDownloadResponse response = downloadFileUseCase.downloadFile(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new InputStreamResource(response.inputStream()));
    }

    /**
     * 파일 업로드 응답 DTO
     */
    public record FileUploadResponseDto(
            String fileName,
            String fileUrl,
            long size
    ) {}
}
