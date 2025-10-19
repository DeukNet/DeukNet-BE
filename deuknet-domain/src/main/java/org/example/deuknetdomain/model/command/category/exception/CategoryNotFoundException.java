package org.example.deuknetdomain.model.command.category.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Category를 찾을 수 없을 때 발생하는 예외
 */
public class CategoryNotFoundException extends DomainException {

    public CategoryNotFoundException() {
        super(404, "CATEGORY_NOT_FOUND", "Category not found");
    }
}
