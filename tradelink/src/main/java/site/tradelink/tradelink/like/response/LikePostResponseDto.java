package site.tradelink.tradelink.like.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikePostResponseDto {
    private int acceptedCount;

    public static LikePostResponseDto accepted(int count) {
        return new LikePostResponseDto(count);
    }
}
