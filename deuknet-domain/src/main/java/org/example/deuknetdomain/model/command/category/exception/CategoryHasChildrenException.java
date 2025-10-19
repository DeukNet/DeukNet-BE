package org.example.deuknetdomain.model.command.category.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 하위 Category가 있는 Category를 삭제하려고 할 때 발생하는 예외
 */
public class CategoryHasChildrenException extends DomainException {

    public CategoryHasChildrenException() {
        super(409, "CATEGORY_HAS_CHILDREN", "Cannot delete category with children");
    }
}
