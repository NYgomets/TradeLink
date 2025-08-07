package site.tradelink.tradelink.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import site.tradelink.tradelink.comment.entity.Comment;
import site.tradelink.tradelink.comment.response.CommentDto;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.entity.UploadFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostDto {
    private Long seq;
    private String title;
    private String content;
    private List<CommentDto> comments;
    private List<UploadFileDto> uploadFiles;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH시 mm분 ss초", timezone = "Asia/Seoul")
    private LocalDateTime createTime;

    /**
     * 1. LazyInitializationException 주의
     * 2. N+1 문제 주의
     */
    public PostDto(Post post, List<Comment> comments, List<UploadFile> uploadFiles) {
        this.seq = post.getSeq();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.comments = comments == null ? new ArrayList<>() :
                comments.stream()
                        .map(CommentDto::new)
                        .collect(Collectors.toList());
        this.uploadFiles = uploadFiles == null ? new ArrayList<>() :
                uploadFiles.stream()
                        .map(UploadFileDto::new)
                        .collect(Collectors.toList());
        this.name = post.getMember().getMemberName();
        this.createTime = post.getCreateTime();
    }
}
