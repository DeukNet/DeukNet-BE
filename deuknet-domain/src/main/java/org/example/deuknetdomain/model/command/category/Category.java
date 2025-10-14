package org.example.deuknetdomain.model.command.category;

import lombok.Getter;
import org.example.deuknetdomain.common.vo.CategoryName;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Category {
    private final UUID id;
    private CategoryName name;
    private final UUID parentCategoryId;

    private Category(UUID id, CategoryName name, UUID parentCategoryId) {
        this.id = id;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }

    public static Category create(CategoryName name, UUID parentCategoryId) {
        return new Category(UUID.randomUUID(), name, parentCategoryId);
    }

    public static Category restore(UUID id, CategoryName name, UUID parentCategoryId) {
        return new Category(id, name, parentCategoryId);
    }

    public void changeName(CategoryName name) {
        this.name = name;
    }

    public boolean isRootCategory() {
        return parentCategoryId == null;
    }

    public Optional<UUID> getParentCategoryId() {
        return Optional.ofNullable(parentCategoryId);
    }
}
