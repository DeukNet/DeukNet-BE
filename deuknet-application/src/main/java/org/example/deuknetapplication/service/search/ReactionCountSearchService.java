package org.example.deuknetapplication.service.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.ReactionCountSearchResponse;
import org.example.deuknetapplication.port.out.search.ReactionCountSearchPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ReactionCount 검색 서비스
 *
 * Elasticsearch를 활용한 리액션 집계 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ReactionCountSearchService {

    private final ReactionCountSearchPort reactionCountSearchPort;

    public Optional<ReactionCountSearchResponse> findByTargetId(UUID targetId) {
        return reactionCountSearchPort.findByTargetId(targetId);
    }

    public List<ReactionCountSearchResponse> findByTargetIds(List<UUID> targetIds) {
        return reactionCountSearchPort.findByTargetIds(targetIds);
    }

    public List<ReactionCountSearchResponse> findMostLiked(int page, int size) {
        return reactionCountSearchPort.findMostLiked(page, size);
    }

    public List<ReactionCountSearchResponse> findMostReacted(int page, int size) {
        return reactionCountSearchPort.findMostReacted(page, size);
    }

    public List<ReactionCountSearchResponse> findByMinLikeCount(long minLikeCount, int page, int size) {
        return reactionCountSearchPort.findByMinLikeCount(minLikeCount, page, size);
    }

    public List<ReactionCountSearchResponse> findByMinTotalCount(long minTotalCount, int page, int size) {
        return reactionCountSearchPort.findByMinTotalCount(minTotalCount, page, size);
    }

    public List<ReactionCountSearchResponse> findRecentlyUpdated(int page, int size) {
        return reactionCountSearchPort.findRecentlyUpdated(page, size);
    }

    public long getTotalReactionCount() {
        return reactionCountSearchPort.getTotalReactionCount();
    }
}
