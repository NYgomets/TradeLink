package site.tradelink.tradelink.post.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponseDto {
    private Long postSeq;
    private String title;
    private String content;
    private String authorName;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
