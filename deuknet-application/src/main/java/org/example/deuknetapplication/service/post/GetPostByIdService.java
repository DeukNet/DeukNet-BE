package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.GetPostByIdUseCase;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.repository.*;
import org.example.deuknetdomain.domain.post.Post;
import org.example.deuknetdomain.domain.post.PostCategoryAssignment;
import org.example.deuknetdomain.domain.post.exception.PostNotFoundException;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetPostByIdService implements GetPostByIdUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCategoryAssignmentRepository categoryAssignmentRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;

    public GetPostByIdService(
            PostRepository postRepository,
            UserRepository userRepository,
            PostCategoryAssignmentRepository categoryAssignmentRepository,
            CategoryRepository categoryRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryAssignmentRepository = categoryAssignmentRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
    }

    @Override
    public PostSearchResponse getPostById(UUID postId) {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // 2. 작성자 정보 조회
        User author = userRepository.findById(post.getAuthorId())
                .orElse(null);

        // 3. 카테고리 정보 조회
        List<PostCategoryAssignment> assignments = categoryAssignmentRepository.findByPostId(postId);
        List<UUID> categoryIds = assignments.stream()
                .map(PostCategoryAssignment::getCategoryId)
                .collect(Collectors.toList());

        List<String> categoryNames = categoryIds.stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .map(category -> category.getName().getValue())
                        .orElse(null))
                .filter(name -> name != null)
                .collect(Collectors.toList());

        // 4. 댓글 수 조회
        long commentCount = commentRepository.countByPostId(postId);

        // 5. 좋아요 수 조회
        long likeCount = reactionRepository.countByTargetIdAndReactionType(postId, ReactionType.LIKE);

        // 6. PostSearchResponse 생성
        return new PostSearchResponse(
                post.getId(),
                post.getTitle().getValue(),
                post.getContent().getValue(),
                post.getAuthorId(),
                author != null ? author.getUsername() : null,
                author != null ? author.getDisplayName() : null,
                post.getStatus().name(),
                categoryIds,
                categoryNames,
                post.getViewCount(),
                commentCount,
                likeCount,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
