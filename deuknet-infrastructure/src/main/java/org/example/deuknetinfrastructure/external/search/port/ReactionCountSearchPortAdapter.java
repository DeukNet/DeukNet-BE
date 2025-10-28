package org.example.deuknetinfrastructure.external.search.port;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.ReactionCountSearchResponse;
import org.example.deuknetapplication.port.out.search.ReactionCountSearchPort;
import org.example.deuknetinfrastructure.external.search.adapter.ReactionCountSearchAdapter;
import org.example.deuknetinfrastructure.external.search.document.ReactionCountDocument;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 리액션 카운트 검색 Port Adapter
 */
@Component
@RequiredArgsConstructor
public class ReactionCountSearchPortAdapter implements ReactionCountSearchPort {

    private final ReactionCountSearchAdapter reactionCountSearchAdapter;

    @Override
    public Optional<ReactionCountSearchResponse> findByTargetId(UUID targetId) {
        return reactionCountSearchAdapter.findByTargetId(targetId).map(this::toResponse);
    }

    @Override
    public List<ReactionCountSearchResponse> findByTargetIds(List<UUID> targetIds) {
        return reactionCountSearchAdapter.findByTargetIds(targetIds)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReactionCountSearchResponse> findMostLiked(int page, int size) {
        return reactionCountSearchAdapter.findMostLiked(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReactionCountSearchResponse> findMostReacted(int page, int size) {
        return reactionCountSearchAdapter.findMostReacted(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReactionCountSearchResponse> findByMinLikeCount(long minLikeCount, int page, int size) {
        return reactionCountSearchAdapter.findByMinLikeCount(minLikeCount, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReactionCountSearchResponse> findByMinTotalCount(long minTotalCount, int page, int size) {
        return reactionCountSearchAdapter.findByMinTotalCount(minTotalCount, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReactionCountSearchResponse> findRecentlyUpdated(int page, int size) {
        return reactionCountSearchAdapter.findRecentlyUpdated(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalReactionCount() {
        return reactionCountSearchAdapter.getTotalReactionCount();
    }

    private ReactionCountSearchResponse toResponse(ReactionCountDocument document) {
        Map<String, Long> reactionCounts = new HashMap<>();
        reactionCounts.put("LIKE", document.getLikeCount());
        reactionCounts.put("DISLIKE", document.getDislikeCount());

        return new ReactionCountSearchResponse(
                document.getId(),
                document.getLikeCount(),
                0L, // loveCount not in document
                0L, // laughCount not in document
                0L, // wowCount not in document
                0L, // sadCount not in document
                0L, // angryCount not in document
                document.getTotalCount(),
                reactionCounts,
                document.getLastEventTimestamp()
        );
    }
}
