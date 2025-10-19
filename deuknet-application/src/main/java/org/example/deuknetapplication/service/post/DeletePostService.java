package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.common.exception.OwnerMismatchException;
import org.example.deuknetapplication.common.exception.ResourceNotFoundException;
import org.example.deuknetapplication.port.in.post.DeletePostUseCase;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.post.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeletePostService implements DeletePostUseCase {

    private final PostRepository postRepository;
    private final CurrentUserPort currentUserPort;

    public DeletePostService(PostRepository postRepository, CurrentUserPort currentUserPort) {
        this.postRepository = postRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void deletePost(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Post post = postRepository.findById(postId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!post.getAuthorId().equals(currentUserId)) {
            throw new OwnerMismatchException();
        }

        post.delete();
        postRepository.save(post);
    }
}
