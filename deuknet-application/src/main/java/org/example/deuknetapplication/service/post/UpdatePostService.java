package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.UpdatePostCommand;
import org.example.deuknetapplication.port.in.post.UpdatePostUseCase;
import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.common.exception.EntityNotFoundException;
import org.example.deuknetdomain.common.exception.ForbiddenException;
import org.example.deuknetdomain.model.command.post.Post;
import org.example.deuknetdomain.model.command.post.PostCategoryAssignment;
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

        Post post = postRepository.findById(command.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Post"));

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new ForbiddenException("Not authorized to update this post");
        }

        post.updateContent(
                org.example.deuknetdomain.common.vo.Title.from(command.getTitle()),
                org.example.deuknetdomain.common.vo.Content.from(command.getContent())
        );
        postRepository.save(post);

        postCategoryAssignmentRepository.deleteByPostId(command.getPostId());

        if (command.getCategoryIds() != null && !command.getCategoryIds().isEmpty()) {
            for (UUID categoryId : command.getCategoryIds()) {
                PostCategoryAssignment assignment = PostCategoryAssignment.create(
                        command.getPostId(),
                        categoryId
                );
                postCategoryAssignmentRepository.save(assignment);
            }
        }
    }
}
