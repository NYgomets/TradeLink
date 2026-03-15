package site.tradelink.tradelink.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import site.tradelink.tradelink.comment.common.enums.CommentStatus;
import site.tradelink.tradelink.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponseDto (
    Long commentSeq,
    String content,
    String authorName,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH시 mm분 ss초", timezone = "Asia/Seoul")
    LocalDateTime createTime,
    int depth,
    List<CommentResponseDto> replies
) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getSeq(),
                comment.getStatus() == CommentStatus.DELETED ? "삭제된 댓글입니다." : comment.getContent(),
                comment.getMember().getMemberName(),
                comment.getCreateTime(),
                comment.getDepth(),
                new ArrayList<>()
        );
    }
}
