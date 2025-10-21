package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.CreatePostApplicationRequest;
import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.out.event.DataChangeEventPublisher;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.post.Post;
import org.example.deuknetdomain.model.command.post.PostCategoryAssignment;
import org.example.deuknetdomain.model.query.post.PostDetailProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final CurrentUserPort currentUserPort;
    private final DataChangeEventPublisher dataChangeEventPublisher;

    public CreatePostService(
            PostRepository postRepository,
            PostCategoryAssignmentRepository postCategoryAssignmentRepository,
            CurrentUserPort currentUserPort,
            DataChangeEventPublisher dataChangeEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
        this.currentUserPort = currentUserPort;
        this.dataChangeEventPublisher = dataChangeEventPublisher;
    }

    @Override
    public UUID createPost(CreatePostApplicationRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Post post = Post.create(
                org.example.deuknetdomain.common.vo.Title.from(request.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(request.getContent()),
                currentUserId
        );

        post = postRepository.save(post);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (UUID categoryId : request.getCategoryIds()) {
                PostCategoryAssignment assignment = PostCategoryAssignment.create(
                        post.getId(),
                        categoryId
                );
                postCategoryAssignmentRepository.save(assignment);
            }
        }

        // 데이터 변경 이벤트 발행 (PostDetailProjection 사용)
        PostDetailProjection projection = new PostDetailProjection(
                post.getId(),
                post.getTitle().getValue(),
                post.getContent().getValue(),
                post.getAuthorId(),
                null,  // authorUsername - TODO: User 정보 조회 필요
                null,  // authorDisplayName - TODO: User 정보 조회 필요
                null,  // authorAvatarUrl - TODO: User 정보 조회 필요
                post.getStatus().name(),
                post.getViewCount(),
                LocalDateTime.now(),  // createdAt
                LocalDateTime.now(),  // updatedAt
                request.getCategoryIds(),
                0L,  // commentCount - 초기값
                0L   // likeCount - 초기값
        );

        dataChangeEventPublisher.publish("PostCreated", post.getId(), projection);

        return post.getId();
    }
}
