package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.PublishPostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetapplication.projection.post.PostSummaryProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PublishPostService implements PublishPostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public PublishPostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public void publishPost(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Post post = postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new OwnerMismatchException();
        }

        post.publish();
        postRepository.save(post);

        // 데이터 변경 이벤트 발행 (상태만 변경되므로 status 업데이트)
        LocalDateTime now = LocalDateTime.now();

        // User 정보 조회
        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(ResourceNotFoundException::new);

        // 1. PostDetail 업데이트 이벤트 발행
        PostDetailProjection detailProjection = PostDetailProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .content(post.getContent().getValue())
                .authorId(post.getAuthorId())
                .authorUsername(author.getUsername())
                .authorDisplayName(author.getDisplayName())
                .authorAvatarUrl(author.getAvatarUrl())
                .status(post.getStatus().name())  // PUBLISHED로 변경됨
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(now)
                .categoryIds(List.of())  // TODO: 실제 조회 필요
                .commentCount(0L)  // TODO
                .likeCount(0L)  // TODO
                .build();
        dataChangeEventPublisher.publish("PostPublished", post.getId(), detailProjection);

        // 2. PostSummary 업데이트 이벤트 발행
        PostSummaryProjection summaryProjection = PostSummaryProjection.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .authorId(post.getAuthorId())
                .authorDisplayName(author.getDisplayName())
                .status(post.getStatus().name())  // PUBLISHED로 변경됨
                .viewCount(post.getViewCount())
                .commentCount(0L)  // TODO
                .createdAt(post.getCreatedAt())
                .updatedAt(now)
                .build();
        dataChangeEventPublisher.publish("PostPublished", post.getId(), summaryProjection);
    }
}
