package org.example.deuknetdomain.domain.reaction.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Reaction을 찾을 수 없을 때 발생하는 예외
 */
public class ReactionNotFoundException extends DomainException {

    public ReactionNotFoundException() {
        super(404, "REACTION_NOT_FOUND", "Reaction not found");
    }
}
