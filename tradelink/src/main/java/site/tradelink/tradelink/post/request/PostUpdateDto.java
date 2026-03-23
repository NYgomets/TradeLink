package site.tradelink.tradelink.post.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDto {
    private String title;
    private String content;
    private List<String> s3Keys;
}
