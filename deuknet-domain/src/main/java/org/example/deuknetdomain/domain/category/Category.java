package org.example.deuknetdomain.domain.category;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.AggregateRoot;
import org.example.deuknetdomain.common.vo.CategoryName;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Category extends AggregateRoot {

    private CategoryName name;
    private final UUID parentCategoryId;
    private String description;
    private String thumbnailImageUrl;
    private UUID ownerId;  // null일 경우 admin만 수정 가능

    private Category(UUID id, CategoryName name, UUID parentCategoryId, String description, String thumbnailImageUrl, UUID ownerId) {
        super(id);
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.ownerId = ownerId;
    }

    public static Category create(CategoryName name, UUID parentCategoryId, String description, String thumbnailImageUrl, UUID ownerId) {
        return new Category(UuidCreator.getTimeOrderedEpoch(), name, parentCategoryId, description, thumbnailImageUrl, ownerId);
    }

    public static Category restore(UUID id, CategoryName name, UUID parentCategoryId, String description, String thumbnailImageUrl, UUID ownerId) {
        return new Category(id, name, parentCategoryId, description, thumbnailImageUrl, ownerId);
    }

    public void changeName(CategoryName name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public void updateOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean hasOwner() {
        return ownerId != null;
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    public boolean isRootCategory() {
        return parentCategoryId == null;
    }

    public Optional<UUID> getParentCategoryId() {
        return Optional.ofNullable(parentCategoryId);
    }
}
