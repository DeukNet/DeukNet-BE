package org.example.deuknetpresentation.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사용자 프로필 수정 요청")
public class UpdateUserProfileRequest {

    @Schema(description = "닉네임", example = "홍길동")
    private String displayName;

    @Schema(description = "자기소개", example = "안녕하세요!")
    private String bio;

    @Schema(description = "아바타 URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
