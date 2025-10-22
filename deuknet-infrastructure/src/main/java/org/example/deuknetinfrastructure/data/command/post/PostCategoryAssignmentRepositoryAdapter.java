package org.example.deuknetinfrastructure.data.command.post;

import org.example.deuknetapplication.port.out.repository.PostCategoryAssignmentRepository;
import org.example.deuknetdomain.domain.post.PostCategoryAssignment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PostCategoryAssignmentRepositoryAdapter implements PostCategoryAssignmentRepository {

    private final JpaPostCategoryAssignmentRepository jpaRepository;
    private final PostCategoryAssignmentMapper mapper;

    public PostCategoryAssignmentRepositoryAdapter(
            JpaPostCategoryAssignmentRepository jpaRepository,
            PostCategoryAssignmentMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PostCategoryAssignment save(PostCategoryAssignment assignment) {
        PostCategoryAssignmentEntity entity = mapper.toEntity(assignment);
        PostCategoryAssignmentEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByPostId(UUID postId) {
        jpaRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostCategoryAssignment> findByPostId(UUID postId) {
        return jpaRepository.findByPostId(postId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
