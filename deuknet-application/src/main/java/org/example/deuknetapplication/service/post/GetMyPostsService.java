package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.GetMyPostsUseCase;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.in.post.SortType;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 내 게시글 조회 서비스
 *
 * 책임:
 * - 현재 사용자 조회
 * - 내 게시글 검색 요청 생성
 */
@Service
@Transactional(readOnly = true)
public class GetMyPostsService implements GetMyPostsUseCase {

    private final SearchPostUseCase searchPostUseCase;
    private final CurrentUserPort currentUserPort;

    public GetMyPostsService(
            SearchPostUseCase searchPostUseCase,
            CurrentUserPort currentUserPort
    ) {
        this.searchPostUseCase = searchPostUseCase;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public PageResponse<PostSearchResponse> getMyPosts(int page, int size) {
        // CurrentUserPort로 현재 사용자 ID 조회
        UUID currentUserId = currentUserPort.getCurrentUserId();

        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(currentUserId)
                .sortType(SortType.RECENT)
                .page(page)
                .size(size)
                .includeAnonymous(true)  // 내 게시물 조회 시 익명 포함
                .build();

        return searchPostUseCase.search(request);
    }
}
