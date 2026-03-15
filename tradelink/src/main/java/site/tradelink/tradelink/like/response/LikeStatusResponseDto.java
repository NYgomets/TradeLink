package site.tradelink.tradelink.like.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeStatusResponseDto {
    private Long postSeq;
    private Boolean isLiked;
    private Long likeCount;

    public static LikeStatusResponseDto of(Long postSeq, Boolean isLiked, Long likeCount) {
        return new LikeStatusResponseDto(postSeq, isLiked, likeCount);
    }
}
