package org.example.deuknetapplication.port.in.category;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CreateCategoryApplicationRequest {

    private String name;
    private UUID parentCategoryId;

    protected CreateCategoryApplicationRequest() {
    }

    public CreateCategoryApplicationRequest(String name, UUID parentCategoryId) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }
}
