package org.example.deuknetpresentation.controller.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetpresentation.controller.comment.dto.CreateCommentRequest;
import org.example.deuknetpresentation.controller.comment.dto.UpdateCommentRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Comment", description = "댓글 관리 API")
public interface CommentApi {

    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글을 작성합니다. parentCommentId를 지정하면 대댓글로 작성됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = UUID.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    UUID createComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "댓글 작성 정보",
                    required = true
            )
            @RequestBody CreateCommentRequest request
    );

    @Operation(
            summary = "댓글 수정",
            description = "작성한 댓글을 수정합니다. 작성자만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    void updateComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "댓글 수정 정보",
                    required = true
            )
            @RequestBody UpdateCommentRequest request
    );

    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다. 작성자만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    void deleteComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable UUID commentId
    );
}
