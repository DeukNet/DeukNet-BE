package org.example.deuknetpresentation.controller.category.dto;

import java.util.UUID;

public class UpdateCategoryRequest {
    private String name;
    private UUID parentCategoryId;

    public UpdateCategoryRequest() {
    }

    public UpdateCategoryRequest(String name, UUID parentCategoryId) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(UUID parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}
