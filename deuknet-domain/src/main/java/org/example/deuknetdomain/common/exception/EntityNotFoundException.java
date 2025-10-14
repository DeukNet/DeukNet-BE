package org.example.deuknetdomain.common.exception;

public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String entityName) {
        super(CommonErrorCode.ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super(CommonErrorCode.ENTITY_NOT_FOUND);
    }
}
