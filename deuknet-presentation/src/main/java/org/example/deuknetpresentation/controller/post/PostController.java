package org.example.deuknetpresentation.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetapplication.port.in.post.*;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Post", description = "게시글 관리 API")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final PublishPostUseCase publishPostUseCase;
    private final DeletePostUseCase deletePostUseCase;

    public PostController(
            CreatePostUseCase createPostUseCase,
            UpdatePostUseCase updatePostUseCase,
            PublishPostUseCase publishPostUseCase,
            DeletePostUseCase deletePostUseCase
    ) {
        this.createPostUseCase = createPostUseCase;
        this.updatePostUseCase = updatePostUseCase;
        this.publishPostUseCase = publishPostUseCase;
        this.deletePostUseCase = deletePostUseCase;
    }

    @Operation(
            summary = "게시글 작성",
            description = "새로운 게시글을 작성합니다. 초안(DRAFT) 상태로 생성되며, 여러 카테고리를 지정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = UUID.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<UUID> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 작성 정보",
                    required = true
            )
            @RequestBody CreatePostRequest request
    ) {
        CreatePostUseCase.CreatePostCommand command = new CreatePostUseCase.CreatePostCommand(
                Title.from(request.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(request.getContent()),
                request.getCategoryIds()
        );
        
        UUID postId = createPostUseCase.createPost(command);
        return ResponseEntity.ok(postId);
    }

    @Operation(
            summary = "게시글 수정",
            description = "작성한 게시글을 수정합니다. 작성자만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 수정 정보",
                    required = true
            )
            @RequestBody UpdatePostRequest request
    ) {
        UpdatePostUseCase.UpdatePostCommand command = new UpdatePostUseCase.UpdatePostCommand(
                postId,
                Title.from(request.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(request.getContent()),
                request.getCategoryIds()
        );
        
        updatePostUseCase.updatePost(command);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 발행",
            description = "초안 상태의 게시글을 발행합니다. 발행 후에는 모든 사용자가 볼 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 발행 성공"),
            @ApiResponse(responseCode = "400", description = "이미 발행된 게시글"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId
    ) {
        publishPostUseCase.publishPost(postId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다. Soft Delete 방식으로 상태만 변경됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId
    ) {
        deletePostUseCase.deletePost(postId);
        return ResponseEntity.ok().build();
    }
}
