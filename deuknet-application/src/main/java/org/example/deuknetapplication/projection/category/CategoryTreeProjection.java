package org.example.deuknetapplication.projection.category;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Projection;

import java.util.List;
import java.util.UUID;

/**
 * 카테고리 트리 구조 조회용 Projection
 *
 * 카테고리를 계층 구조로 표현할 때 사용합니다.
 * 하위 카테고리 목록을 포함하여 한 번에 조회합니다.
 */
@Getter
public class CategoryTreeProjection extends Projection {

    private final String name;
    private final UUID parentCategoryId;

    // 하위 카테고리 목록
    private final List<CategoryTreeProjection> children;

    // 통계 정보
    private final Long postCount;
    private final Long totalPostCount;  // 하위 카테고리 포함

    public CategoryTreeProjection(UUID id, String name, UUID parentCategoryId,
                                  List<CategoryTreeProjection> children,
                                  Long postCount, Long totalPostCount) {
        super(id);
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.children = children;
        this.postCount = postCount;
        this.totalPostCount = totalPostCount;
    }
}
