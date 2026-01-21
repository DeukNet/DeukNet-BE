package org.example.deuknetpresentation.controller.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 익명 권한 상태 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "익명 권한 상태 응답")
public class AnonymousAccessStatusResponse {

    @Schema(description = "익명 접근 권한 보유 여부", example = "true")
    private boolean hasAnonymousAccess;

    /**
     * 정적 팩토리 메서드
     */
    public static AnonymousAccessStatusResponse of(boolean hasAnonymousAccess) {
        return new AnonymousAccessStatusResponse(hasAnonymousAccess);
    }
}
