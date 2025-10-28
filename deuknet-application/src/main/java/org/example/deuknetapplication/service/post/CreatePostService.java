package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.CreatePostApplicationRequest;
import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostCountProjection;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.user.User;
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
 *   - PostCategoryAssignmentService: 카테고리 할당
 *   - PostProjectionFactory: Projection 객체 생성
 *   - PostEventPublisher: 이벤트 발행
 */
@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;
    private final PostCategoryAssignmentService categoryAssignmentService;
    private final PostProjectionFactory projectionFactory;
    private final PostEventPublisher eventPublisher;

    public CreatePostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort,
            PostCategoryAssignmentService categoryAssignmentService,
            PostProjectionFactory projectionFactory,
            PostEventPublisher eventPublisher
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.currentUserPort = currentUserPort;
        this.categoryAssignmentService = categoryAssignmentService;
        this.projectionFactory = projectionFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UUID createPost(CreatePostApplicationRequest request) {
        User author = getCurrentUser();

        Post post = createAndSavePost(request, author.getId());

        categoryAssignmentService.assignCategories(post.getId(), request.getCategoryIds());

        PostDetailProjection detailProjection = projectionFactory.createDetailProjectionForCreation(
                post, author, request.getCategoryIds()
        );
        PostCountProjection countProjection = projectionFactory.createCountProjectionForCreation(post);

        eventPublisher.publishPostCreated(post.getId(), detailProjection, countProjection);

        return post.getId();
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
     * Post Aggregate 생성 및 저장
     * 이것만이 이 서비스의 핵심 책임입니다.
     */
    private Post createAndSavePost(CreatePostApplicationRequest request, UUID authorId) {
        Post post = Post.create(
                Title.from(request.getTitle()),
                Content.from(request.getContent()),
                authorId
        );
        return postRepository.save(post);
    }
}
