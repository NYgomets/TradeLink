package site.tradelink.tradelink.like.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import site.tradelink.tradelink.like.common.enums.ActionType;

@Getter
public class LikePostDto {
    @NotNull(message = "게시글 ID는 필수이다.")
    private Long postSeq;

    @NotNull(message = "액션 타입은 필수이다.")
    private ActionType actionType;
}
