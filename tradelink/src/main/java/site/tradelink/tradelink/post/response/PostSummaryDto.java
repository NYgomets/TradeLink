package site.tradelink.tradelink.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import site.tradelink.tradelink.post.entity.Post;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostSummaryDto {

    private Long seq;
    private String title;
    private String authorName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH시 mm분 ss초", timezone = "Asia/Seoul")
    private LocalDateTime createTime;
    private int commentCount;
    private boolean hasFiles;

    /**
     * 1. LazyInitializationException 주의
     * 2. N+1 문제 주의
     */
    public PostSummaryDto(Post post, int commentCount, int fileCount) {
        this.seq = post.getSeq();
        this.title = post.getTitle();
        this.authorName = post.getMember().getMemberName();
        this.createTime = post.getCreateTime();
        this.commentCount = commentCount;
        this.hasFiles = fileCount > 0;
    }
}
