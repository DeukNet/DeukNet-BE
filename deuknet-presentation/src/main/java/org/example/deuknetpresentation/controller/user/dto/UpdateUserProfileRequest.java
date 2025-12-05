package org.example.deuknetpresentation.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사용자 프로필 수정 요청")
public class UpdateUserProfileRequest {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하이어야 합니다")
    @Schema(description = "닉네임 (1-10자)", example = "홍길동")
    private String displayName;

    @Size(max = 500, message = "자기소개는 500자 이하이어야 합니다")
    @Schema(description = "자기소개", example = "안녕하세요!")
    private String bio;

    @Schema(description = "아바타 URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
