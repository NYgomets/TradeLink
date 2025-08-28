package site.tradelink.tradelink.post.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreSignedUrlDto {
    private String preSignedUrl;
    private String s3Key;
}
