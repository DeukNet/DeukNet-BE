package org.example.deuknetinfrastructure.data.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByParentCategoryId(UUID parentCategoryId);
    List<CategoryEntity> findByParentCategoryIdIsNull();
    Optional<CategoryEntity> findByName(String name);
    Page<CategoryEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
