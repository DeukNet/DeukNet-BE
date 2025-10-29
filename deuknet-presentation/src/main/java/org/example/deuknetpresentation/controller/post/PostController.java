package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.port.in.post.*;
import org.example.deuknetapplication.service.post.PostSearchService;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController implements PostApi {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final PublishPostUseCase publishPostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final IncrementViewCountUseCase incrementViewCountUseCase;
    private final PostSearchService postSearchService;

    public PostController(
            CreatePostUseCase createPostUseCase,
            UpdatePostUseCase updatePostUseCase,
            PublishPostUseCase publishPostUseCase,
            DeletePostUseCase deletePostUseCase,
            IncrementViewCountUseCase incrementViewCountUseCase,
            PostSearchService postSearchService
    ) {
        this.createPostUseCase = createPostUseCase;
        this.updatePostUseCase = updatePostUseCase;
        this.publishPostUseCase = publishPostUseCase;
        this.deletePostUseCase = deletePostUseCase;
        this.incrementViewCountUseCase = incrementViewCountUseCase;
        this.postSearchService = postSearchService;
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

    @Override
    @PostMapping("/{postId}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void incrementViewCount(@PathVariable UUID postId) {
        incrementViewCountUseCase.incrementViewCount(postId);
    }

    @Override
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PostSearchResponse> getPostById(@PathVariable UUID id) {
        Optional<PostSearchResponse> result = postSearchService.findById(id);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostSearchResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        if (size > 100) {
            size = 100;
        }

        PostSearchRequest request = PostSearchRequest.builder()
                .keyword(keyword)
                .authorId(authorId)
                .categoryId(categoryId)
                .status(status)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();

        List<PostSearchResponse> results = postSearchService.search(request);
        return ResponseEntity.ok(results);
    }

    @Override
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostSearchResponse>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        List<PostSearchResponse> results = postSearchService.findPopularPosts(page, size);
        return ResponseEntity.ok(results);
    }
}
