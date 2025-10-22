package org.example.deuknetdomain.domain.category.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Category 계층 구조에서 순환 참조가 발생할 때 발생하는 예외
 */
public class CategoryCircularReferenceException extends DomainException {

    public CategoryCircularReferenceException() {
        super(400, "CIRCULAR_REFERENCE", "Circular reference detected in category hierarchy");
    }
}
