package site.tradelink.tradelink.like.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.like.request.LikePostDto;
import site.tradelink.tradelink.like.response.LikePostResponseDto;
import site.tradelink.tradelink.like.service.LikeService;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/likes")
public class LikeAuthController {

    private final LikeService likeService;

    /**
     * 좋아요 / 좋아요 취소
     */
    @PostMapping
    public ApiResponse<LikePostResponseDto> toggleLike(@AuthenticationPrincipal Long memberSeq, @RequestBody @Valid LikePostDto request) {
        LikePostResponseDto response = likeService.toggleLike(memberSeq, request.getPostSeq(), request.getActionType());
        return ApiResponse.ok(response);
    }
}
