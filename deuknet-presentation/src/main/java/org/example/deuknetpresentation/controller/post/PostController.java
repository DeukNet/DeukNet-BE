package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.port.in.post.*;
import org.example.deuknetpresentation.controller.post.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController implements PostApi {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final PublishPostUseCase publishPostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final SearchPostUseCase searchPostUseCase;
    private final GetMyLikedPostsUseCase getMyLikedPostsUseCase;
    private final org.example.deuknetapplication.port.out.security.CurrentUserPort currentUserPort;

    public PostController(
            CreatePostUseCase createPostUseCase,
            UpdatePostUseCase updatePostUseCase,
            PublishPostUseCase publishPostUseCase,
            DeletePostUseCase deletePostUseCase,
            GetPostUseCase getPostUseCase,
            SearchPostUseCase searchPostUseCase,
            GetMyLikedPostsUseCase getMyLikedPostsUseCase,
            org.example.deuknetapplication.port.out.security.CurrentUserPort currentUserPort
    ) {
        this.createPostUseCase = createPostUseCase;
        this.updatePostUseCase = updatePostUseCase;
        this.publishPostUseCase = publishPostUseCase;
        this.deletePostUseCase = deletePostUseCase;
        this.getPostUseCase = getPostUseCase;
        this.searchPostUseCase = searchPostUseCase;
        this.getMyLikedPostsUseCase = getMyLikedPostsUseCase;
        this.currentUserPort = currentUserPort;
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
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PostSearchResponse> getPostById(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "false") boolean forceCommandModel
    ) {
        PostSearchResponse response = getPostUseCase.getPostById(id, forceCommandModel);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<PostSearchResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(defaultValue = "RECENT") String sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        // 페이지 크기 제한: 최대 100
        if (size > 100) {
            size = 100;
        }

        // String sortType을 SortType enum으로 변환
        SortType sortTypeEnum;
        try {
            sortTypeEnum = SortType.valueOf(sortType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 잘못된 값이면 기본값(RECENT) 사용
            sortTypeEnum = SortType.RECENT;
        }

        PostSearchRequest request = PostSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .authorId(authorId)
                .sortType(sortTypeEnum)
                .page(page)
                .size(size)
                .includeAnonymous(authorId == null)  // authorId 지정 시에만 익명 제외
                .build();

        PageResponse<PostSearchResponse> results = searchPostUseCase.search(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<PostSearchResponse>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 페이지 크기 제한: 최대 100
        if (size > 100) {
            size = 100;
        }

        // 현재 사용자 ID로 조회 (익명 게시물 포함)
        UUID currentUserId = currentUserPort.getCurrentUserId();

        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(currentUserId)
                .sortType(SortType.RECENT)
                .page(page)
                .size(size)
                .includeAnonymous(true)  // 내 게시물 조회 시 익명 포함
                .build();

        PageResponse<PostSearchResponse> results = searchPostUseCase.search(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/liked")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<PostSearchResponse>> getMyLikedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 페이지 크기 제한: 최대 100
        if (size > 100) {
            size = 100;
        }

        PageResponse<PostSearchResponse> results = getMyLikedPostsUseCase.getMyLikedPosts(page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/featured")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<PostSearchResponse>> getFeaturedPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 페이지 크기 제한: 최대 20
        if (size > 20) {
            size = 20;
        }

        PageResponse<PostSearchResponse> results = searchPostUseCase.findFeaturedPosts(categoryId, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggest")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> suggestKeywords(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 최대 제안 수 제한
        if (size > 20) {
            size = 20;
        }

        List<String> suggestions = searchPostUseCase.suggestKeywords(q, size);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/trending")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostSearchResponse>> getTrendingPosts() {
        List<PostSearchResponse> results = searchPostUseCase.findTrendingPosts(10);
        return ResponseEntity.ok(results);
    }
}
