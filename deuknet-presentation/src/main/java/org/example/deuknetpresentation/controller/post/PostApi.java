package org.example.deuknetpresentation.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            description = "특정 게시글을 ID로 조회합니다."
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
            @PathVariable UUID id
    );

    @Operation(
            summary = "게시글 검색",
            description = "여러 조건으로 게시글을 검색합니다. 모든 필터는 AND로 결합됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = PostSearchResponse.class))
            )
    })
    ResponseEntity<List<PostSearchResponse>> searchPosts(
            @Parameter(description = "검색 키워드 (제목 + 내용)") @RequestParam(required = false) String keyword,
            @Parameter(description = "작성자 ID") @RequestParam(required = false) UUID authorId,
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "게시글 상태 (DRAFT, PUBLISHED, DELETED)") @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (createdAt, viewCount 등)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 순서 (asc, desc)") @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(
            summary = "인기 게시글 조회",
            description = "조회수 기준으로 인기 게시글을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostSearchResponse.class))
            )
    })
    ResponseEntity<List<PostSearchResponse>> getPopularPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size
    );
}
