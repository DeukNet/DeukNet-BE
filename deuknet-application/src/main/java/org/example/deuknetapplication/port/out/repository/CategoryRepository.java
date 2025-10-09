package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.model.command.category.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findByParentCategoryId(UUID parentCategoryId);
    List<Category> findRootCategories();
    Optional<Category> findByName(String name);
    void delete(Category category);
}
