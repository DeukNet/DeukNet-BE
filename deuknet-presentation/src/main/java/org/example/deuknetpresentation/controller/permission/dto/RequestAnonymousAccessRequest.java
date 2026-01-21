package org.example.deuknetpresentation.controller.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 익명 권한 신청 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "익명 권한 신청 요청")
public class RequestAnonymousAccessRequest {

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "익명 권한 비밀번호", example = "secret_password", required = true)
    private String password;
}
