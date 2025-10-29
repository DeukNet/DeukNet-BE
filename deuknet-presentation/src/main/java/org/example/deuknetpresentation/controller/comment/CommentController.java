package org.example.deuknetpresentation.controller.comment;

import org.example.deuknetapplication.port.in.comment.*;
import org.example.deuknetpresentation.controller.comment.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController implements CommentApi {

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

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createComment(@PathVariable UUID postId, @RequestBody CreateCommentRequest request) {
        request.setPostId(postId);
        return createCommentUseCase.createComment(request);
    }

    @Override
    @PutMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateComment(@PathVariable UUID postId, @PathVariable UUID commentId, @RequestBody UpdateCommentRequest request) {
        request.setCommentId(commentId);
        updateCommentUseCase.updateComment(request);
    }

    @Override
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID postId, @PathVariable UUID commentId) {
        deleteCommentUseCase.deleteComment(commentId);
    }
}
