package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public interface CreatePostUseCase {
    UUID createPost(CreatePostApplicationRequest request);
}
