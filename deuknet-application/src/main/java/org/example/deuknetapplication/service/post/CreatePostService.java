package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetapplication.port.in.post.CreatePostApplicationRequest;
import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.domain.permission.exception.AnonymousAccessDeniedException;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Post 생성 유스케이스 구현체 (진짜 SRP 준수)
 *
 * 단일 책임: Post 생성 유스케이스의 흐름을 조정(orchestration)
 *
 * - Post 생성 도메인 로직만 직접 처리
 * - 나머지 책임은 각 전문 서비스에 위임
 *   - PostProjectionFactory: Projection 객체 생성
 */
@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final PostProjectionFactory projectionFactory;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public CreatePostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort,
            PostProjectionFactory projectionFactory,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
        this.projectionFactory = projectionFactory;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public UUID createPost(CreatePostApplicationRequest request) {
        User author = getCurrentUser();

        // 익명 작성 권한 검증
        validateAnonymousAccess(author, request.getAuthorType());

        Post post = createAndSavePost(request, author.getId());

        publishPostCreated(post, author);

        log.info("[POST_CREATED] postId={}, authorId={}, username={}, title={}, categoryId={}",
                post.getId(),
                author.getId(),
                author.getUsername(),
                request.getTitle(),
                request.getCategoryId());

        return post.getId();
    }

    /**
     * 익명 작성 권한 검증
     * 익명 작성 시도 시 권한이 없으면 예외 발생
     */
    private void validateAnonymousAccess(User user, AuthorType authorType) {
        if (AuthorType.ANONYMOUS.equals(authorType) && !user.isCanAccessAnonymous()) {
            log.warn("[ANONYMOUS_ACCESS_DENIED] userId={}, username={}",
                    user.getId(), user.getUsername());
            throw new AnonymousAccessDeniedException();
        }
    }

    private void publishPostCreated(Post post, User author) {
        PostDetailProjection detailProjection = projectionFactory.createDetailProjectionForCreation(
                post, post.getCategoryId()
        );

        dataChangeEventPublisher.publish(EventType.POST_CREATED, post.getId(), detailProjection);
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser() {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * PostService
     * Post Aggregate 생성 및 저장
     * 이것만이 이 서비스의 핵심 책임입니다.
     */
    private Post createAndSavePost(CreatePostApplicationRequest request, UUID authorId) {
        Post post = Post.create(
                Title.from(request.getTitle()),
                Content.from(request.getContent()),
                authorId,
                request.getCategoryId(),
                request.getAuthorType(),
                request.getThumbnailImageUrl()
        );
        return postRepository.save(post);
    }
}
