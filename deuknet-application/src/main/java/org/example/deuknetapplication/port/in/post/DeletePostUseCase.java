package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public interface DeletePostUseCase {
    void deletePost(UUID postId);
}
