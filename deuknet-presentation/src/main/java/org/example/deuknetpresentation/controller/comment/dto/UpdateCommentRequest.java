package org.example.deuknetpresentation.controller.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.comment.UpdateCommentApplicationRequest;

import java.util.UUID;

@Schema(description = "댓글 수정 요청")
public class UpdateCommentRequest extends UpdateCommentApplicationRequest {

    public UpdateCommentRequest() {
        super();
    }

    public UpdateCommentRequest(UUID commentId, String content) {
        super(commentId, content);
    }

    @Override
    @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다.", required = true)
    public String getContent() {
        return super.getContent();
    }
}
