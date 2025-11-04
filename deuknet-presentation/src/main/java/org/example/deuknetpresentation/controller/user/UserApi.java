package org.example.deuknetpresentation.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetpresentation.controller.user.dto.UpdateUserProfileRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(
            summary = "사용자 프로필 수정",
            description = "로그인한 사용자의 프로필(닉네임, 소개, 아바타)을 수정합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/me")
    void updateProfile(@RequestBody UpdateUserProfileRequest request);
}
