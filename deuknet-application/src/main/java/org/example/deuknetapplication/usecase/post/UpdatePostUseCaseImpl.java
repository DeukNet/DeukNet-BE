package org.example.deuknetapplication.usecase.post;

import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.post.post.Post;
import org.example.deuknetdomain.model.command.post.postcategory.PostCategoryAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdatePostUseCaseImpl implements UpdatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final CurrentUserPort currentUserPort;

    public UpdatePostUseCaseImpl(
            PostRepository postRepository,
            PostCategoryAssignmentRepository postCategoryAssignmentRepository,
            CurrentUserPort currentUserPort
    ) {
        this.postRepository = postRepository;
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void updatePost(UpdatePostCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        
        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new IllegalArgumentException("Not authorized to update this post");
        }
        
        post.updateContent(command.title(), command.content());
        postRepository.save(post);
        
        postCategoryAssignmentRepository.deleteByPostId(command.postId());
        
        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            for (UUID categoryId : command.categoryIds()) {
                PostCategoryAssignment assignment = PostCategoryAssignment.create(
                        command.postId(),
                        categoryId
                );
                postCategoryAssignmentRepository.save(assignment);
            }
        }
    }
}
