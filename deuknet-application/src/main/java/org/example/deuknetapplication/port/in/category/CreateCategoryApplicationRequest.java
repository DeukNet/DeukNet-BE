package org.example.deuknetapplication.port.in.category;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CreateCategoryApplicationRequest {

    private String name;
    private UUID parentCategoryId;
    private String description;
    private String thumbnailImageUrl;

    protected CreateCategoryApplicationRequest() {
    }

    public CreateCategoryApplicationRequest(String name, UUID parentCategoryId, String description, String thumbnailImageUrl) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }
}
