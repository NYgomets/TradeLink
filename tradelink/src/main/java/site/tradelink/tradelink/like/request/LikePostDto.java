package site.tradelink.tradelink.like.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import site.tradelink.tradelink.like.common.enums.ActionType;

import java.util.List;

@Getter
public class LikePostDto {

    @NotEmpty(message = "좋아요 액션은 최소 1개 이상이어야 한다.")
    @Valid
    private List<LikeAction> actions;

    @Getter
    public static class LikeAction {
        @NotNull(message = "게시글 ID는 필수이다.")
        private Long postSeq;

        @NotNull(message = "액션 타입은 필수이다.")
        private ActionType actionType;
    }
}
