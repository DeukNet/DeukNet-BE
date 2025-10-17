package org.example.deuknetdomain.common.seedwork;

import java.util.UUID;

public abstract class AggregateRoot extends Entity {

    public AggregateRoot(UUID id) {
        super(id);
    }
}
