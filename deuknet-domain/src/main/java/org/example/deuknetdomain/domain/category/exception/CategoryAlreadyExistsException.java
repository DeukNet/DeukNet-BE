package org.example.deuknetdomain.domain.category.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 동일한 이름의 Category가 이미 존재할 때 발생하는 예외
 */
public class CategoryAlreadyExistsException extends DomainException {

    public CategoryAlreadyExistsException() {
        super(409, "CATEGORY_ALREADY_EXISTS", "Category name already exists");
    }
}
