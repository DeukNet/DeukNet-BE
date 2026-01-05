package org.example.deuknetdomain.domain.category.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 카테고리 삭제 권한이 없을 때 발생하는 예외
 */
public class CategoryDeleteNotAllowedException extends DomainException {

    public CategoryDeleteNotAllowedException() {
        super(403, "CATEGORY_DELETE_NOT_ALLOWED", "You don't have permission to delete this category");
    }
}
