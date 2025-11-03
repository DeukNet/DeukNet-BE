package org.example.deuknetpresentation.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.example.deuknetpresentation.controller.auth.dto.RefreshTokenRequest;
import org.example.deuknetpresentation.controller.auth.dto.TokenResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(
            summary = "Google OAuth 로그인 시작",
            description = "Google OAuth 인증 페이지로 리다이렉트합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Google OAuth 페이지로 리다이렉트")
    })
    void initiateGoogleOAuth(HttpServletResponse response) throws IOException;

    @Operation(
            summary = "Google OAuth 콜백",
            description = "Google OAuth 인증 후 콜백을 처리하고 프론트엔드로 리다이렉트합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "프론트엔드로 리다이렉트 (토큰 포함)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    RedirectView handleGoogleCallback(@RequestParam("code") String code, @RequestParam("state") String state);

    @Operation(
            summary = "토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    TokenResponse refresh(@RequestBody RefreshTokenRequest request);
}
