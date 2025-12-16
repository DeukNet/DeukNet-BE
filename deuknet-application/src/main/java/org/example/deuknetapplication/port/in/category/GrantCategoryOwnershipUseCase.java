package org.example.deuknetapplication.port.in.category;

import java.util.UUID;

public interface GrantCategoryOwnershipUseCase {
    void grantOwnership(UUID categoryId, UUID targetUserId);
}
