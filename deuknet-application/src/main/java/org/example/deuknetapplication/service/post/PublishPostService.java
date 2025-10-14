package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PublishPostUseCase;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.common.exception.EntityNotFoundException;
import org.example.deuknetdomain.common.exception.ForbiddenException;
import org.example.deuknetdomain.model.command.post.post.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PublishPostService implements PublishPostUseCase {

    private final PostRepository postRepository;
    private final CurrentUserPort currentUserPort;

    public PublishPostService(PostRepository postRepository, CurrentUserPort currentUserPort) {
        this.postRepository = postRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void publishPost(UUID postId) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post"));
        
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new ForbiddenException("Not authorized to publish this post");
        }
        
        post.publish();
        postRepository.save(post);
    }
}
