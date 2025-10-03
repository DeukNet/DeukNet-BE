package org.example.deuknetdomain.model.command.category;

import java.util.Optional;
import java.util.UUID;

public class Category {

    private final UUID id;
    private String name;
    private final UUID parentCategoryId;

    private Category(UUID id, String name, UUID parentCategoryId) {
        this.id = id;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }

    public static Category create(String name, UUID parentCategoryId) {
        validateName(name);
        return new Category(UUID.randomUUID(), name, parentCategoryId);
    }

    public static Category restore(UUID id, String name, UUID parentCategoryId) {
        return new Category(id, name, parentCategoryId);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
    }

    public void changeName(String name) {
        validateName(name);
        this.name = name;
    }

    public boolean isRootCategory() {
        return parentCategoryId == null;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<UUID> getParentCategoryId() {
        return Optional.ofNullable(parentCategoryId);
    }
}
