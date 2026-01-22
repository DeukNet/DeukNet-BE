package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.in.post.DeletePostUseCase;
import org.example.deuknetapplication.port.in.post.GetMyLikedPostsUseCase;
import org.example.deuknetapplication.port.in.post.GetMyPostsUseCase;
import org.example.deuknetapplication.port.in.post.GetPostUseCase;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.PublishPostUseCase;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.in.post.SortType;
import org.example.deuknetapplication.port.in.post.UpdatePostUseCase;
import org.example.deuknetpresentation.controller.post.dto.CreatePostRequest;
import org.example.deuknetpresentation.controller.post.dto.UpdatePostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    private final GetMyPostsUseCase getMyPostsUseCase;
    private final GetMyLikedPostsUseCase getMyLikedPostsUseCase;

    public PostController(
            CreatePostUseCase createPostUseCase,
            UpdatePostUseCase updatePostUseCase,
            PublishPostUseCase publishPostUseCase,
            DeletePostUseCase deletePostUseCase,
            GetPostUseCase getPostUseCase,
            SearchPostUseCase searchPostUseCase,
            GetMyPostsUseCase getMyPostsUseCase,
            GetMyLikedPostsUseCase getMyLikedPostsUseCase
    ) {
        this.createPostUseCase = createPostUseCase;
        this.updatePostUseCase = updatePostUseCase;
        this.publishPostUseCase = publishPostUseCase;
        this.deletePostUseCase = deletePostUseCase;
        this.getPostUseCase = getPostUseCase;
        this.searchPostUseCase = searchPostUseCase;
        this.getMyPostsUseCase = getMyPostsUseCase;
        this.getMyLikedPostsUseCase = getMyLikedPostsUseCase;
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
            // TODO: InvalidSortTypeException을 throw하는 것을 고려할 수 있지만,
            // 현재는 사용자 편의를 위해 기본값 사용
            sortTypeEnum = SortType.RECENT;
        }

        PostSearchRequest request = PostSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .authorId(authorId)
                .sortType(sortTypeEnum)
                .page(page)
                .size(size)
                // includeAnonymous는 기본값(true) 사용
                // Service Layer에서 CurrentUserPort로 권한 확인하여 자동 필터링
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

        // Service에서 현재 사용자 조회 및 검색 처리
        PageResponse<PostSearchResponse> results = getMyPostsUseCase.getMyPosts(page, size);
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
