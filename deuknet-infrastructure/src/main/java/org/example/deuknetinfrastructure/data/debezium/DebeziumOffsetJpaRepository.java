package org.example.deuknetinfrastructure.data.debezium;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Debezium Offset JPA Repository
 */
public interface DebeziumOffsetJpaRepository extends JpaRepository<DebeziumOffsetEntity, String> {
}
