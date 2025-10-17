package org.example.deuknetdomain.common.seedwork;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Projection implements Persistable {

    private final UUID id;

    public Projection(UUID id) {
        this.id = id;
    }
}
