package org.example.deuknetpresentation.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetpresentation.controller.post.dto.CreatePostRequest;
import org.example.deuknetpresentation.controller.post.dto.UpdatePostRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Post", description = "게시글 관리 API")
public interface PostApi {

    @Operation(
            summary = "게시글 작성",
            description = "새로운 게시글을 작성합니다. 초안(DRAFT) 상태로 생성되며, 여러 카테고리를 지정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = UUID.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    UUID createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 작성 정보",
                    required = true
            )
            @RequestBody CreatePostRequest request
    );

    @Operation(
            summary = "게시글 수정",
            description = "작성한 게시글을 수정합니다. 작성자만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    void updatePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 수정 정보",
                    required = true
            )
            @RequestBody UpdatePostRequest request
    );

    @Operation(
            summary = "게시글 발행",
            description = "초안 상태의 게시글을 발행합니다. 발행 후에는 모든 사용자가 볼 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 발행 성공"),
            @ApiResponse(responseCode = "400", description = "이미 발행된 게시글"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    void publishPost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId
    );

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다. Soft Delete 방식으로 상태만 변경됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    void deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId
    );

    // incrementViewCount() 메서드 제거
    // 조회수는 POST /api/posts/{postId}/reactions (ReactionType.VIEW)로 처리

    @Operation(
            summary = "게시글 ID로 조회",
            description = "특정 게시글을 ID로 조회합니다. forceCommandModel=true로 설정하면 PostgreSQL에서 직접 조회하여 최신 데이터를 보장합니다 (생성/수정 직후 사용)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 조회 성공",
                    content = @Content(schema = @Schema(implementation = PostSearchResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<PostSearchResponse> getPostById(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "PostgreSQL 직접 조회 여부 (기본값: false, Elasticsearch 우선 조회)", required = false)
            @RequestParam(required = false, defaultValue = "false") boolean forceCommandModel
    );

    @Operation(
            summary = "게시글 검색 (통합)",
            description = "게시글을 검색합니다. sortType으로 최신순/인기순을 선택할 수 있습니다. 항상 PUBLISHED 상태만 조회됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    ResponseEntity<PageResponse<PostSearchResponse>> searchPosts(
            @Parameter(description = "검색 키워드 (제목 + 내용)") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "작성자 ID") @RequestParam(required = false) UUID authorId,
            @Parameter(description = "정렬 타입 (RECENT: 최신순, POPULAR: 인기순)") @RequestParam(defaultValue = "RECENT") String sortType,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "내 게시물 조회",
            description = "현재 로그인한 사용자가 작성한 게시물을 조회합니다. 익명 게시물도 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<PageResponse<PostSearchResponse>> getMyPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "개념글 조회",
            description = "좋아요가 많은 게시글 상위 20개를 조회합니다. 카테고리별 필터링이 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    ResponseEntity<PageResponse<PostSearchResponse>> getFeaturedPosts(
            @Parameter(description = "카테고리 ID (선택사항)") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 20)") @RequestParam(defaultValue = "20") int size
    );
}
