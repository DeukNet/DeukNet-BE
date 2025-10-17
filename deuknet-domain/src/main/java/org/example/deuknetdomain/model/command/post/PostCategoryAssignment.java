package org.example.deuknetdomain.model.command.post;

import java.util.UUID;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Entity;

@Getter
public class PostCategoryAssignment extends Entity {

    private final UUID postId;
    private final UUID categoryId;

    private PostCategoryAssignment(UUID id, UUID postId, UUID categoryId) {
        super(id);
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
