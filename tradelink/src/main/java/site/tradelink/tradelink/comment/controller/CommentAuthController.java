package site.tradelink.tradelink.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.comment.request.CommentCreateDto;
import site.tradelink.tradelink.comment.request.CommentUpdateDto;
import site.tradelink.tradelink.comment.service.CommentService;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/posts/{postSeq}/comments")
public class CommentAuthController {

    private final CommentService commentService;

    /**
     * 댓글 생성
     */
    @PostMapping
    public ApiResponse<Long> createComment(@PathVariable Long postSeq, @AuthenticationPrincipal Long memberSeq, @RequestBody CommentCreateDto request) {
        Long commentSeq = commentService.createComment(postSeq, memberSeq, request);
        return ApiResponse.ok(commentSeq);
    }

    /**
     * 댓글 수정
     */
    @PatchMapping("/{commentSeq}")
    public ApiResponse<Void> updateComment(@PathVariable Long postSeq, @PathVariable Long commentSeq, @AuthenticationPrincipal Long memberSeq, @RequestBody CommentUpdateDto request) {
        commentService.updateComment(commentSeq, memberSeq, request);
        return ApiResponse.ok(null);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    @DeleteMapping("/{commentSeq}")
    public ApiResponse<Void> deleteComment(@PathVariable Long postSeq, @PathVariable Long commentSeq, @AuthenticationPrincipal Long memberSeq) {
        commentService.deleteComment(commentSeq, memberSeq);
        return ApiResponse.ok(null);
    }
}
