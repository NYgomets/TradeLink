package site.tradelink.tradelink.comment.request;

import lombok.Getter;

@Getter
public class CommentCreateDto {
    private String content;
    private Long parentCommentSeq;
}
