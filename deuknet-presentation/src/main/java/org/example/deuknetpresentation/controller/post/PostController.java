package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.port.in.post.*;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController implements PostApi {

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

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createPost(@RequestBody CreatePostRequest request) {
        return createPostUseCase.createPost(request);
    }

    @Override
    @PutMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePost(@PathVariable UUID postId, @RequestBody UpdatePostRequest request) {
        request.setPostId(postId);
        updatePostUseCase.updatePost(request);
    }

    @Override
    @PostMapping("/{postId}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishPost(@PathVariable UUID postId) {
        publishPostUseCase.publishPost(postId);
    }

    @Override
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID postId) {
        deletePostUseCase.deletePost(postId);
    }
}
