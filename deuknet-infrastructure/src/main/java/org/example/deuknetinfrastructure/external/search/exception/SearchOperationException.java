package org.example.deuknetinfrastructure.external.search.exception;

import org.example.deuknetinfrastructure.common.exception.InfrastructureException;

/**
 * Elasticsearch 검색 작업 실패 시 발생하는 예외
 * 검색, 조회, 인덱싱 등의 Elasticsearch 작업 중 발생하는 오류를 처리합니다.
 */
public class SearchOperationException extends InfrastructureException {

    public SearchOperationException(String message) {
        super(500, "SEARCH_OPERATION_FAILED", message);
    }

    public SearchOperationException(String message, Throwable cause) {
        super(500, "SEARCH_OPERATION_FAILED", message + ": " + cause.getMessage());
    }
}
