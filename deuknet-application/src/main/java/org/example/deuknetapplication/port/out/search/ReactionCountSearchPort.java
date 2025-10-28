package org.example.deuknetapplication.port.out.search;

import org.example.deuknetapplication.dto.search.ReactionCountSearchResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 리액션 카운트 검색 Port
 */
public interface ReactionCountSearchPort {
    Optional<ReactionCountSearchResponse> findByTargetId(UUID targetId);
    List<ReactionCountSearchResponse> findByTargetIds(List<UUID> targetIds);
    List<ReactionCountSearchResponse> findMostLiked(int page, int size);
    List<ReactionCountSearchResponse> findMostReacted(int page, int size);
    List<ReactionCountSearchResponse> findByMinLikeCount(long minLikeCount, int page, int size);
    List<ReactionCountSearchResponse> findByMinTotalCount(long minTotalCount, int page, int size);
    List<ReactionCountSearchResponse> findRecentlyUpdated(int page, int size);
    long getTotalReactionCount();
}
