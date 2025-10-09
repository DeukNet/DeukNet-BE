package org.example.deuknetinfrastructure.data.command.reaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReactionRepository extends JpaRepository<ReactionEntity, UUID> {
}
