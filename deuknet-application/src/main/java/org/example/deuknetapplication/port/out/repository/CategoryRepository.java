package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    List<Category> findByParentCategoryId(UUID parentCategoryId);
    List<Category> findRootCategories();
    Optional<Category> findByName(String name);
    Page<Category> searchByKeyword(String keyword, Pageable pageable);
    void delete(Category category);
}
