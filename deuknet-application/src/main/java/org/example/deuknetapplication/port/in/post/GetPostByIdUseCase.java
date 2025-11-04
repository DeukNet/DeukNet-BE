package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public interface GetPostByIdUseCase {
    PostSearchResponse getPostById(UUID postId);
}
