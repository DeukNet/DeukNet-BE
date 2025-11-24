package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.out.post.PostSearchPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 검색 유스케이스 구현
 * SRP: PostSearchPort로 위임하여 검색 로직 처리
 */
@Service
@Transactional(readOnly = true)
public class SearchPostService implements SearchPostUseCase {

    private final PostSearchPort postSearchPort;

    public SearchPostService(PostSearchPort postSearchPort) {
        this.postSearchPort = postSearchPort;
    }

    @Override
    public Optional<PostSearchResponse> findById(UUID postId) {
        return postSearchPort.findById(postId);
    }

    @Override
    public PageResponse<PostSearchResponse> search(PostSearchRequest request) {
        return postSearchPort.search(request);
    }

    @Override
    public PageResponse<PostSearchResponse> findPopularPosts(int page, int size, UUID categoryId) {
        return postSearchPort.findPopularPosts(page, size, categoryId);
    }
}
