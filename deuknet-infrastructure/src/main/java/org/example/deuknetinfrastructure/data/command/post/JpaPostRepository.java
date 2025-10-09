package org.example.deuknetinfrastructure.data.command.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPostRepository extends JpaRepository<PostEntity, UUID> {
}
