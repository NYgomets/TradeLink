package site.tradelink.tradelink.post.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long postSeq;
    private String title;
    private String content;
    private String authorName;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
