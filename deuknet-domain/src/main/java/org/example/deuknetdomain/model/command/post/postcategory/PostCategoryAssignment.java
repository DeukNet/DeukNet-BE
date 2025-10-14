package org.example.deuknetdomain.model.command.post.postcategory;

import java.util.UUID;
import lombok.Getter;

@Getter
public class PostCategoryAssignment {

    private final UUID id;
    private final UUID postId;
    private final UUID categoryId;

    private PostCategoryAssignment(UUID id, UUID postId, UUID categoryId) {
        this.id = id;
        this.postId = postId;
        this.categoryId = categoryId;
    }

    public static PostCategoryAssignment create(UUID postId, UUID categoryId) {
        return new PostCategoryAssignment(UUID.randomUUID(), postId, categoryId);
    }

    public static PostCategoryAssignment restore(UUID id, UUID postId, UUID categoryId) {
        return new PostCategoryAssignment(id, postId, categoryId);
    }
}
