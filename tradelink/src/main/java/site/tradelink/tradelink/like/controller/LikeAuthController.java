package site.tradelink.tradelink.like.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.like.request.LikePostDto;
import site.tradelink.tradelink.like.response.LikePostResponseDto;
import site.tradelink.tradelink.like.service.LikeCommandService;
import site.tradelink.tradelink.like.service.LikeQueryService;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/likes")
public class LikeAuthController {

    private final LikeCommandService likeCommandService;
    private final LikeQueryService likeQueryService;

    /**
     * 좋아요 / 좋아요 취소 이벤트 적재
     */
    @PostMapping
    public ApiResponse<LikePostResponseDto> batchLike(@AuthenticationPrincipal Long memberSeq, @RequestBody @Valid LikePostDto request) {
        LikePostResponseDto response = likeCommandService.processBatchLikePostEvents(memberSeq, request);
        return ApiResponse.ok(response);
    }

    /**
     * 내가 좋아요한 게시글 목록 조회
     */
    @GetMapping("/me")
    public ApiResponse<Page<Long>> getMyLikedPosts(@AuthenticationPrincipal Long memeberSeq, @PageableDefault Pageable pageable) {
        Page<Long> response = likeQueryService.getMyLikedPostSeqs(memeberSeq, pageable);
        return ApiResponse.ok(response);
    }
}
