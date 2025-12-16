package org.example.deuknetapplication.port.in.category;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateCategoryApplicationRequest {

    private String description;
    private String thumbnailImageUrl;

    protected UpdateCategoryApplicationRequest() {
    }

    public UpdateCategoryApplicationRequest(String description, String thumbnailImageUrl) {
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }
}
