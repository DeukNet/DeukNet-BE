package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.CreatePostCommand;
import org.example.deuknetapplication.port.in.post.CreatePostUseCase;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.post.Post;
import org.example.deuknetdomain.model.command.post.PostCategoryAssignment;
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
                org.example.deuknetdomain.common.vo.Title.from(command.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(command.getContent()),
                currentUserId
        );

        post = postRepository.save(post);

        if (command.getCategoryIds() != null && !command.getCategoryIds().isEmpty()) {
            for (UUID categoryId : command.getCategoryIds()) {
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
