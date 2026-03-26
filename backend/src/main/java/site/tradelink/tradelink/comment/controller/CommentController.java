package site.tradelink.tradelink.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.tradelink.tradelink.comment.response.CommentResponseDto;
import site.tradelink.tradelink.comment.service.CommentService;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postSeq}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 게시글의 댓글 목록 조회
     */
    @GetMapping
    public ApiResponse<List<CommentResponseDto>> getComments(@PathVariable Long postSeq) {
        List<CommentResponseDto> comments = commentService.getCommentsForPost(postSeq);
        return ApiResponse.ok(comments);
    }
}
