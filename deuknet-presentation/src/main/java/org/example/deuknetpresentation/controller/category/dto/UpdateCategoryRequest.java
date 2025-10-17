package org.example.deuknetpresentation.controller.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.category.UpdateCategoryCommand;

import java.util.UUID;

@Schema(description = "카테고리 수정 요청")
public class UpdateCategoryRequest extends UpdateCategoryCommand {

    public UpdateCategoryRequest() {
        super();
    }

    public UpdateCategoryRequest(UUID categoryId, String name) {
        super(categoryId, name);
    }

    @Override
    @Schema(description = "카테고리 이름", example = "기술 & IT", required = true)
    public String getName() {
        return super.getName();
    }
}
