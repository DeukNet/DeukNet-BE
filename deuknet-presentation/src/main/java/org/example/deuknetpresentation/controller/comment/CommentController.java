package org.example.deuknetpresentation.controller.comment;

import org.example.deuknetapplication.port.in.comment.CommentResponse;
import org.example.deuknetapplication.port.in.comment.CreateCommentUseCase;
import org.example.deuknetapplication.port.in.comment.DeleteCommentUseCase;
import org.example.deuknetapplication.port.in.comment.GetCommentsUseCase;
import org.example.deuknetapplication.port.in.comment.UpdateCommentUseCase;
import org.example.deuknetpresentation.controller.comment.dto.CreateCommentRequest;
import org.example.deuknetpresentation.controller.comment.dto.UpdateCommentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController implements CommentApi {

    private final CreateCommentUseCase createCommentUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final GetCommentsUseCase getCommentsUseCase;

    public CommentController(
            CreateCommentUseCase createCommentUseCase,
            UpdateCommentUseCase updateCommentUseCase,
            DeleteCommentUseCase deleteCommentUseCase,
            GetCommentsUseCase getCommentsUseCase
    ) {
        this.createCommentUseCase = createCommentUseCase;
        this.updateCommentUseCase = updateCommentUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.getCommentsUseCase = getCommentsUseCase;
    }

    @GetMapping
    public List<CommentResponse> getComments(@PathVariable UUID postId) {
        return getCommentsUseCase.getCommentsByPostId(postId);
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
