package org.example.deuknetpresentation.controller.reaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetpresentation.controller.reaction.dto.AddReactionRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Reaction", description = "리액션 관리 API")
public interface ReactionApi {

    @Operation(
            summary = "리액션 추가",
            description = "게시글에 리액션(좋아요, 사랑해요 등)을 추가합니다. " +
                    "리액션 타입: LIKE, LOVE, HAHA, WOW, SAD, ANGRY"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "리액션 추가 성공",
                    content = @Content(schema = @Schema(implementation = UUID.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 리액션 타입)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    UUID addReaction(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리액션 타입 정보 (LIKE, LOVE, HAHA, WOW, SAD, ANGRY)",
                    required = true
            )
            @RequestBody AddReactionRequest request
    );

    @Operation(
            summary = "리액션 삭제",
            description = "자신이 추가한 리액션을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리액션 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인의 리액션이 아님)"),
            @ApiResponse(responseCode = "404", description = "리액션을 찾을 수 없음")
    })
    void removeReaction(
            @Parameter(description = "리액션 ID", required = true)
            @PathVariable UUID reactionId
    );
}
