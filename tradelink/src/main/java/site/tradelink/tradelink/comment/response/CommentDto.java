package site.tradelink.tradelink.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import site.tradelink.tradelink.comment.entity.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {
    private Long seq;
    private String content;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH시 mm분 ss초", timezone = "Asia/Seoul")
    private LocalDateTime createTime;

    public CommentDto(Comment comment) {
        this.seq = comment.getSeq();
        this.content = comment.getContent();
        this.name = comment.getMember().getMemberName();
        this.createTime = comment.getCreateTime();
    }
}
