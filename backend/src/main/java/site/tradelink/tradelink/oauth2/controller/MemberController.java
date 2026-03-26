package site.tradelink.tradelink.oauth2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.tradelink.tradelink.oauth2.common.principal.CustomOAuth2User;
import site.tradelink.tradelink.oauth2.dto.MemberDto;
import site.tradelink.tradelink.oauth2.service.MemberService;
import site.tradelink.tradelink.supports.request.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<MemberDto> getMe(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        Long memberSeq = customOAuth2User.getMemberSeq();
        return ApiResponse.ok(memberService.getMe(memberSeq));
    }
}
