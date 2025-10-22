package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.post.Post;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(UUID id);
    void delete(Post post);
}
