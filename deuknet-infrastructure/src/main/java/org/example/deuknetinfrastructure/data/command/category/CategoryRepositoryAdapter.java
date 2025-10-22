package org.example.deuknetinfrastructure.data.command.category;

import org.example.deuknetapplication.port.out.repository.CategoryRepository;
import org.example.deuknetdomain.domain.category.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;
    private final CategoryMapper mapper;

    public CategoryRepositoryAdapter(JpaCategoryRepository jpaCategoryRepository, CategoryMapper mapper) {
        this.jpaCategoryRepository = jpaCategoryRepository;
        this.mapper = mapper;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = mapper.toEntity(category);
        CategoryEntity savedEntity = jpaCategoryRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaCategoryRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Category> findByParentCategoryId(UUID parentCategoryId) {
        return jpaCategoryRepository.findByParentCategoryId(parentCategoryId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findRootCategories() {
        return jpaCategoryRepository.findByParentCategoryIdIsNull().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findByName(String name) {
        return jpaCategoryRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(Category category) {
        CategoryEntity entity = mapper.toEntity(category);
        jpaCategoryRepository.delete(entity);
    }
}
