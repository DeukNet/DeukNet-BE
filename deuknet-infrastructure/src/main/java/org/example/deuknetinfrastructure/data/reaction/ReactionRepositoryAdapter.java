package org.example.deuknetinfrastructure.data.reaction;

import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ReactionRepositoryAdapter implements ReactionRepository {

    private final JpaReactionRepository jpaReactionRepository;
    private final ReactionMapper mapper;

    public ReactionRepositoryAdapter(JpaReactionRepository jpaReactionRepository, ReactionMapper mapper) {
        this.jpaReactionRepository = jpaReactionRepository;
        this.mapper = mapper;
    }

    @Override
    public Reaction save(Reaction reaction) {
        ReactionEntity entity = mapper.toEntity(reaction);
        ReactionEntity savedEntity = jpaReactionRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Reaction> findById(UUID id) {
        return jpaReactionRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(Reaction reaction) {
        ReactionEntity entity = mapper.toEntity(reaction);
        jpaReactionRepository.delete(entity);
    }

    @Override
    public long countByTargetIdAndReactionType(UUID targetId, ReactionType reactionType) {
        return jpaReactionRepository.countByTargetIdAndReactionType(targetId, reactionType);
    }

    @Override
    public Optional<Reaction> findByTargetIdAndUserIdAndReactionType(UUID targetId, UUID userId, ReactionType reactionType) {
        return jpaReactionRepository.findFirstByTargetIdAndUserIdAndReactionType(targetId, userId, reactionType)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByTargetIdAndUserIdAndReactionType(UUID targetId, UUID userId, ReactionType reactionType) {
        return jpaReactionRepository.existsByTargetIdAndUserIdAndReactionType(targetId, userId, reactionType);
    }

    @Override
    public List<Reaction> findByTargetIdAndUserId(UUID targetId, UUID userId) {
        return jpaReactionRepository.findByTargetIdAndUserId(targetId, userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
