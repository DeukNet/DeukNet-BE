package org.example.deuknetapplication.usecase.post;

import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.model.command.post.post.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeletePostUseCaseImpl implements DeletePostUseCase {

    private final PostRepository postRepository;
    private final CurrentUserPort currentUserPort;

    public DeletePostUseCaseImpl(PostRepository postRepository, CurrentUserPort currentUserPort) {
        this.postRepository = postRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void deletePost(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new IllegalArgumentException("Not authorized to delete this post");
        }
        
        post.delete();
        postRepository.save(post);
    }
}
