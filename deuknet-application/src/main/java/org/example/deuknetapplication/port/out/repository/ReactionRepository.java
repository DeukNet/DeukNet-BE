package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.model.command.reaction.Reaction;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository {
    Reaction save(Reaction reaction);
    Optional<Reaction> findById(UUID id);
    void delete(Reaction reaction);
}
