package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
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
public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;
    private final PostCategoryAssignmentRepository postCategoryAssignmentRepository;
    private final CurrentUserPort currentUserPort;

    public CreatePostService(
            PostRepository postRepository,
            PostCategoryAssignmentRepository postCategoryAssignmentRepository,
            CurrentUserPort currentUserPort
    ) {
        this.postRepository = postRepository;
        this.postCategoryAssignmentRepository = postCategoryAssignmentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public UUID createPost(CreatePostCommand command) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        
        Post post = Post.create(
                command.title(),
                command.content(),
                currentUserId
        );
        
        post = postRepository.save(post);
        
        if (command.categoryIds() != null && !command.categoryIds().isEmpty()) {
            for (UUID categoryId : command.categoryIds()) {
                PostCategoryAssignment assignment = PostCategoryAssignment.create(
                        post.getId(),
                        categoryId
                );
                postCategoryAssignmentRepository.save(assignment);
            }
        }
        
        return post.getId();
    }
}
