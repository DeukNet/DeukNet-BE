package org.example.deuknetdomain.common.seedwork;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class DomainEvent implements Persistable {

    private final UUID id;

    public DomainEvent(UUID id) {
        this.id = id;
    }
}
