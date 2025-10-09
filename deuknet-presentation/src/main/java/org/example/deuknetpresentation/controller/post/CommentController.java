package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.usecase.comment.*;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;

    public CommentController(
            CreateCommentUseCase createCommentUseCase,
            UpdateCommentUseCase updateCommentUseCase,
            DeleteCommentUseCase deleteCommentUseCase
    ) {
        this.createCommentUseCase = createCommentUseCase;
        this.updateCommentUseCase = updateCommentUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
    }

    @PostMapping
    public ResponseEntity<UUID> createComment(
            @PathVariable UUID postId,
            @RequestBody CreateCommentRequest request
    ) {
        CreateCommentUseCase.CreateCommentCommand command = new CreateCommentUseCase.CreateCommentCommand(
                postId,
                Content.from(request.getContent()),
                request.getParentCommentId()
        );
        
        UUID commentId = createCommentUseCase.createComment(command);
        return ResponseEntity.ok(commentId);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable UUID commentId,
            @RequestBody UpdateCommentRequest request
    ) {
        UpdateCommentUseCase.UpdateCommentCommand command = new UpdateCommentUseCase.UpdateCommentCommand(
                commentId,
                Content.from(request.getContent())
        );
        
        updateCommentUseCase.updateComment(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        deleteCommentUseCase.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
