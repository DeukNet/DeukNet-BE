package org.example.deuknetpresentation.controller.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.deuknetapplication.port.in.permission.CheckAnonymousAccessUseCase;
import org.example.deuknetapplication.port.in.permission.RequestAnonymousAccessCommand;
import org.example.deuknetapplication.port.in.permission.RequestAnonymousAccessUseCase;
import org.example.deuknetpresentation.controller.permission.dto.AnonymousAccessStatusResponse;
import org.example.deuknetpresentation.controller.permission.dto.RequestAnonymousAccessRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 권한 관련 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permission", description = "권한 관리 API")
public class PermissionController {

    private final RequestAnonymousAccessUseCase requestAnonymousAccessUseCase;
    private final CheckAnonymousAccessUseCase checkAnonymousAccessUseCase;

    public PermissionController(
            RequestAnonymousAccessUseCase requestAnonymousAccessUseCase,
            CheckAnonymousAccessUseCase checkAnonymousAccessUseCase
    ) {
        this.requestAnonymousAccessUseCase = requestAnonymousAccessUseCase;
        this.checkAnonymousAccessUseCase = checkAnonymousAccessUseCase;
    }

    @Operation(
            summary = "익명 권한 신청",
            description = "비밀번호를 입력하여 익명 작성/조회 권한을 신청합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "권한 부여 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "비밀번호 불일치"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "비밀번호 미설정"
            )
    })
    @PostMapping("/anonymous")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestAnonymousAccess(@Valid @RequestBody RequestAnonymousAccessRequest request) {
        RequestAnonymousAccessCommand command = new RequestAnonymousAccessCommand(request.getPassword());
        requestAnonymousAccessUseCase.requestAnonymousAccess(command);
    }

    @Operation(
            summary = "익명 권한 상태 조회",
            description = "현재 사용자의 익명 접근 권한 보유 여부를 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AnonymousAccessStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/anonymous")
    @ResponseStatus(HttpStatus.OK)
    public AnonymousAccessStatusResponse checkAnonymousAccess() {
        boolean hasAccess = checkAnonymousAccessUseCase.hasAnonymousAccess();
        return AnonymousAccessStatusResponse.of(hasAccess);
    }
}
