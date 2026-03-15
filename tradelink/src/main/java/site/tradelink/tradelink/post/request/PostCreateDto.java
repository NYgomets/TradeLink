package site.tradelink.tradelink.post.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostCreateDto {
    private String title;
    private String content;
    private List<String> s3Keys;
}
