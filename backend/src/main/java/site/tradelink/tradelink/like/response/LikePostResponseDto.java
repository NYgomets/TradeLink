package site.tradelink.tradelink.like.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikePostResponseDto {

    private Boolean isLiked;
    private Long likeCount;

    public static LikePostResponseDto of(Boolean isLiked, Long likeCount) {
        return new LikePostResponseDto(isLiked, likeCount);
    }
}
