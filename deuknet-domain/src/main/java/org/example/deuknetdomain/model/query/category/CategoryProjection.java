package org.example.deuknetdomain.model.query.category;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.util.UUID;

/**
 * 카테고리 조회용 Projection
 *
 * 카테고리 목록 및 상세 조회에 사용됩니다.
 */
@Getter
public class CategoryProjection extends Projection {

    private final String name;
    private final UUID parentCategoryId;
    private final boolean isRootCategory;

    // 통계 정보
    private final Long postCount;

    public CategoryProjection(UUID id, String name, UUID parentCategoryId, boolean isRootCategory, Long postCount) {
        super(id);
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.isRootCategory = isRootCategory;
        this.postCount = postCount;
    }
}
