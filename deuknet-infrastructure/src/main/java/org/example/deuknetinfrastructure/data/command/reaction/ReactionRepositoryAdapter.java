package org.example.deuknetinfrastructure.data.command.reaction;

import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetdomain.model.command.reaction.Reaction;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
}
