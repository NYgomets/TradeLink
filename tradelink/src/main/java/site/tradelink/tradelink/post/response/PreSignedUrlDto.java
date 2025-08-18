package site.tradelink.tradelink.post.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class PreSignedUrlDto {
    private String preSignedUrl;
    private String saveFileName;
}
