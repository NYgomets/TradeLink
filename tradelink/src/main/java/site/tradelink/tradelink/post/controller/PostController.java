package site.tradelink.tradelink.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.post.response.PostResponseDto;
import site.tradelink.tradelink.post.response.PostSummaryDto;
import site.tradelink.tradelink.post.service.PostService;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postSeq}")
    public ApiResponse<PostResponseDto> getPost(@PathVariable Long postSeq) {
        PostResponseDto response = postService.getPost(postSeq);
        return ApiResponse.ok(response);
    }

    @GetMapping
    public ApiResponse<Page<PostSummaryDto>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(postService.getPosts(page, size));
    }
}
