package org.example.deuknetdomain.common.seedwork;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Entity implements Persistable {

    private final UUID id;

    public Entity(UUID id) {
        this.id = id;
    }
}
