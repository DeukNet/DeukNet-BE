package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public interface PublishPostUseCase {
    void publishPost(UUID postId);
}
