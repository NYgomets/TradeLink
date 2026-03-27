package site.tradelink.tradelink.like.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.tradelink.tradelink.like.response.LikeStatusResponseDto;
import site.tradelink.tradelink.like.service.LikeQueryService;
import site.tradelink.tradelink.oauth2.common.principal.CustomOAuth2User;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikeController {

    private final LikeQueryService likeQueryService;

    /**
     * 특정 게시글 좋아요 상태 조회 (게시글 상세용)
     */
    @GetMapping("/posts/{postSeq}")
    public ApiResponse<LikeStatusResponseDto> getLikeStatus(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long postSeq) {
        Long memberSeq = customOAuth2User.getMemberSeq();
        LikeStatusResponseDto response = likeQueryService.getLikeStatus(memberSeq, postSeq);
        return ApiResponse.ok(response);
    }
}
