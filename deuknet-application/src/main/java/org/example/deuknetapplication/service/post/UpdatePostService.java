package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.UpdatePostUseCase;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.common.exception.EntityNotFoundException;
import org.example.deuknetdomain.common.exception.ForbiddenException;
import org.example.deuknetdomain.model.command.post.post.Post;
import org.example.deuknetdomain.model.command.post.postcategory.PostCategoryAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdatePostService implements UpdatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final CurrentUserPort currentUserPort;

    public UpdatePostService(
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
                .orElseThrow(() -> new EntityNotFoundException("Post"));
        
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new ForbiddenException("Not authorized to update this post");
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
