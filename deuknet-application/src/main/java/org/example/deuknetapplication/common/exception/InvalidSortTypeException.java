package org.example.deuknetapplication.common.exception;

/**
 * 잘못된 정렬 타입 예외
 */
public class InvalidSortTypeException extends ApplicationException {
    public InvalidSortTypeException(String sortType) {
        super(400, "INVALID_SORT_TYPE", "Invalid sort type: " + sortType);
    }
}
