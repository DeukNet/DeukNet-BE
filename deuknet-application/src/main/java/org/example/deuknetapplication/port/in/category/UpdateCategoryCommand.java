package org.example.deuknetapplication.port.in.category;

import java.util.UUID;

public class UpdateCategoryCommand {
    private UUID categoryId;
    private String name;

    protected UpdateCategoryCommand() {
    }

    public UpdateCategoryCommand(UUID categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
