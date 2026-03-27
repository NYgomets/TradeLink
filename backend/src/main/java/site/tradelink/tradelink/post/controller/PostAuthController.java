package site.tradelink.tradelink.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.oauth2.common.principal.CustomOAuth2User;
import site.tradelink.tradelink.post.request.PostCreateDto;
import site.tradelink.tradelink.post.request.PostUpdateDto;
import site.tradelink.tradelink.post.response.PreSignedUrlDto;
import site.tradelink.tradelink.post.service.PostService;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/posts")
public class PostAuthController {

    private final PostService postService;

    /**
     * [1단계] 이미지 업로드를 위한 Pre-signed URL 발급
     * @param fileName 원본 파일명 (확장자 추출용)
     * @param contentType MIME 타입 (검증용)
     */
    @GetMapping("/presigned-url")
    public ApiResponse<PreSignedUrlDto> getUploadUrl(@RequestParam String fileName, @RequestParam String contentType) {
        PreSignedUrlDto response = postService.issueUploadUrl(fileName, contentType);
        return ApiResponse.ok(response);
    }

    /**
     * [2단계] 게시글 생성 (S3 업로드 완료 후 호출)
     */
    @PostMapping
    public ApiResponse<Long> createPost(@RequestBody PostCreateDto request, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long memberSeq = customOAuth2User.getMemberSeq();
        Long postSeq = postService.createPost(request, memberSeq);
        return ApiResponse.ok(postSeq);
    }


    /**
     * 게시글 수정
     */
    @PutMapping("/{postSeq}")
    public ApiResponse<Void> updatePost(@PathVariable Long postSeq, @RequestBody PostUpdateDto updateDto, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long memberSeq = customOAuth2User.getMemberSeq();
        postService.updatePost(postSeq, memberSeq, updateDto);
        return ApiResponse.ok(null);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postSeq}")
    public ApiResponse<Void> deletePost(@PathVariable Long postSeq, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long memberSeq = customOAuth2User.getMemberSeq();
        postService.deletePost(postSeq, memberSeq);
        return ApiResponse.ok(null);
    }
}
