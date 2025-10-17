package org.example.deuknetpresentation.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetapplication.port.in.reaction.AddReactionUseCase;
import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.example.deuknetpresentation.controller.post.dto.AddReactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Reaction", description = "리액션 관리 API")
@RestController
@RequestMapping("/api/posts/{postId}/reactions")
public class ReactionController {

    private final AddReactionUseCase addReactionUseCase;
    private final RemoveReactionUseCase removeReactionUseCase;

    public ReactionController(
            AddReactionUseCase addReactionUseCase,
            RemoveReactionUseCase removeReactionUseCase
    ) {
        this.addReactionUseCase = addReactionUseCase;
        this.removeReactionUseCase = removeReactionUseCase;
    }

    @Operation(
            summary = "리액션 추가",
            description = "게시글에 리액션(좋아요, 사랑해요 등)을 추가합니다. " +
                    "리액션 타입: LIKE, LOVE, HAHA, WOW, SAD, ANGRY"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리액션 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 리액션 타입)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addReaction(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable UUID postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "리액션 타입 정보 (LIKE, LOVE, HAHA, WOW, SAD, ANGRY)",
                    required = true
            )
            @RequestBody AddReactionRequest request
    ) {
        AddReactionUseCase.AddReactionCommand command = new AddReactionUseCase.AddReactionCommand(
                postId,
                ReactionType.valueOf(request.getReactionType())
        );

        addReactionUseCase.addReaction(command);
    }

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
    @DeleteMapping("/{reactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(
            @Parameter(description = "리액션 ID", required = true)
            @PathVariable UUID reactionId
    ) {
        removeReactionUseCase.removeReaction(reactionId);
    }
}
