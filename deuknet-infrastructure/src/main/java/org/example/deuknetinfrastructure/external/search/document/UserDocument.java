package org.example.deuknetinfrastructure.external.search.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

/**
 * 사용자 검색용 Elasticsearch Document
 * <br>
 * 사용자 검색 및 프로필 조회에 최적화되었습니다.
 * - username, displayName: 검색 대상
 * - bio: 검색 대상
 * - 통계: 정렬 및 집계
 */
@Getter
@Setter
@Document(indexName = "users")
public class UserDocument extends BaseDocument {

    /**
     * 사용자 이름 (고유, 필터링 및 검색)
     */
    @Field(type = FieldType.Keyword)
    private String username;

    /**
     * 표시 이름 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String displayName;

    /**
     * 자기소개 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String bio;

    /**
     * 아바타 URL (표시용)
     */
    @Field(type = FieldType.Keyword, index = false)
    private String avatarUrl;

    /**
     * 작성한 게시글 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long postCount;

    /**
     * 작성한 댓글 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long commentCount;

    /**
     * 팔로워 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long followerCount;

    protected UserDocument() {
        super();
    }

    public UserDocument(UUID id) {
        super(id);
    }

    public static UserDocument create(UUID id, String username, String displayName,
                                      String bio, String avatarUrl,
                                      Long postCount, Long commentCount, Long followerCount) {
        UserDocument document = new UserDocument(id);
        document.username = username;
        document.displayName = displayName;
        document.bio = bio;
        document.avatarUrl = avatarUrl;
        document.postCount = postCount != null ? postCount : 0L;
        document.commentCount = commentCount != null ? commentCount : 0L;
        document.followerCount = followerCount != null ? followerCount : 0L;
        return document;
    }
}
