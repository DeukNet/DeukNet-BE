package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.usecase.post.*;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping
    public ResponseEntity<UUID> createPost(@RequestBody CreatePostRequest request) {
        CreatePostUseCase.CreatePostCommand command = new CreatePostUseCase.CreatePostCommand(
                Title.from(request.getTitle()),
                Content.from(request.getContent()),
                request.getCategoryIds()
        );
        
        UUID postId = createPostUseCase.createPost(command);
        return ResponseEntity.ok(postId);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable UUID postId,
            @RequestBody UpdatePostRequest request
    ) {
        UpdatePostUseCase.UpdatePostCommand command = new UpdatePostUseCase.UpdatePostCommand(
                postId,
                Title.from(request.getTitle()),
                Content.from(request.getContent()),
                request.getCategoryIds()
        );
        
        updatePostUseCase.updatePost(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(@PathVariable UUID postId) {
        publishPostUseCase.publishPost(postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        deletePostUseCase.deletePost(postId);
        return ResponseEntity.ok().build();
    }
}
