package org.example.deuknetinfrastructure.data.post;

import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetdomain.domain.post.Post;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final JpaPostRepository jpaPostRepository;
    private final PostMapper mapper;

    public PostRepositoryAdapter(JpaPostRepository jpaPostRepository, PostMapper mapper) {
        this.jpaPostRepository = jpaPostRepository;
        this.mapper = mapper;
    }

    @Override
    public Post save(Post post) {
        PostEntity entity = mapper.toEntity(post);
        PostEntity savedEntity = jpaPostRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return jpaPostRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(Post post) {
        PostEntity entity = mapper.toEntity(post);
        jpaPostRepository.delete(entity);
    }
}
