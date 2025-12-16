package org.example.deuknetdomain.domain.category.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 카테고리 수정 권한이 없을 때 발생하는 예외
 */
public class CategoryUpdateNotAllowedException extends DomainException {

    public CategoryUpdateNotAllowedException() {
        super(403, "CATEGORY_UPDATE_NOT_ALLOWED", "You don't have permission to update this category");
    }
}
