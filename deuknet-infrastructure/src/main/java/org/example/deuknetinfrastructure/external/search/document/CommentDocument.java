package org.example.deuknetinfrastructure.external.search.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

/**
 * 댓글 검색용 Elasticsearch Document
 *
 * 댓글 내용 검색 및 특정 게시글/사용자의 댓글 조회에 최적화되었습니다.
 * - 내용: 형태소 분석을 통한 한글 검색
 * - 게시글/작성자: 필터링
 * - 대댓글: 계층 구조 필터링
 */
@Getter
@Setter
@Document(indexName = "comments")
public class CommentDocument extends BaseDocument {

    /**
     * 댓글이 속한 게시글 ID (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String postId;

    /**
     * 댓글 내용 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    /**
     * 작성자 ID (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String authorId;

    /**
     * 작성자 username (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String authorUsername;

    /**
     * 작성자 displayName (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String authorDisplayName;

    /**
     * 부모 댓글 ID (대댓글 필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String parentCommentId;

    /**
     * 대댓글 여부
     */
    @Field(type = FieldType.Boolean)
    private Boolean isReply;

    protected CommentDocument() {
        super();
    }

    public CommentDocument(UUID id) {
        super(id);
    }

    public static CommentDocument create(UUID id, UUID postId, String content,
                                         UUID authorId, String authorUsername, String authorDisplayName,
                                         UUID parentCommentId, Boolean isReply) {
        CommentDocument document = new CommentDocument(id);
        document.postId = postId.toString();
        document.content = content;
        document.authorId = authorId.toString();
        document.authorUsername = authorUsername;
        document.authorDisplayName = authorDisplayName;
        document.parentCommentId = parentCommentId != null ? parentCommentId.toString() : null;
        document.isReply = isReply;
        return document;
    }
}
