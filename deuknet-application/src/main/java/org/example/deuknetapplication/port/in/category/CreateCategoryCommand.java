package org.example.deuknetapplication.port.in.category;

import java.util.UUID;

public class CreateCategoryCommand {
    private String name;
    private UUID parentCategoryId;

    protected CreateCategoryCommand() {
    }

    public CreateCategoryCommand(String name, UUID parentCategoryId) {
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
