package org.example.deuknetpresentation.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.user.GetCurrentUserUseCase;
import org.example.deuknetapplication.port.in.user.GetUserByIdUseCase;
import org.example.deuknetapplication.port.in.user.GetUsersUseCase;
import org.example.deuknetapplication.port.in.user.SearchUsersUseCase;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileCommand;
import org.example.deuknetapplication.port.in.user.UpdateUserProfileUseCase;
import org.example.deuknetapplication.port.in.user.UserResponse;
import org.example.deuknetpresentation.controller.user.dto.UpdateUserProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final GetUsersUseCase getUsersUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final SearchUsersUseCase searchUsersUseCase;

    public UserController(
            GetUsersUseCase getUsersUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            SearchUsersUseCase searchUsersUseCase
    ) {
        this.getUsersUseCase = getUsersUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.searchUsersUseCase = searchUsersUseCase;
    }

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "인증된 현재 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getCurrentUser() {
        return getCurrentUserUseCase.getCurrentUser();
    }

    @Operation(
            summary = "현재 사용자 프로필 수정",
            description = "인증된 현재 사용자의 프로필을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @PutMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                request.getDisplayName(),
                request.getBio(),
                request.getAvatarUrl()
        );
        updateUserProfileUseCase.updateProfile(command);
    }

    @Operation(
            summary = "특정 사용자 정보 조회",
            description = "사용자 ID로 특정 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserById(
            @Parameter(description = "사용자 ID", example = "a2590d1e-6b42-417e-af7e-095a8865c65b")
            @PathVariable UUID userId
    ) {
        return getUserByIdUseCase.getUserById(userId);
    }

    @Operation(
            summary = "사용자 목록 조회",
            description = "페이지 단위로 사용자 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<UserResponse> getUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return getUsersUseCase.getUsers(page, size);
    }

    @Operation(
            summary = "사용자 검색",
            description = "username 또는 displayName으로 사용자를 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<UserResponse> searchUsers(
            @Parameter(description = "검색 키워드", example = "john")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        // 최대 100개로 제한
        if (size > 100) {
            size = 100;
        }
        return searchUsersUseCase.searchUsers(keyword, page, size);
    }
}
