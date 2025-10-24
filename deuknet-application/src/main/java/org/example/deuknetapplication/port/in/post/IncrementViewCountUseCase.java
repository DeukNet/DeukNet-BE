package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public interface IncrementViewCountUseCase {
    void incrementViewCount(UUID postId);
}
