package site.tradelink.tradelink.cryptocurrency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.tradelink.tradelink.cryptocurrency.dto.HoldingDto;
import site.tradelink.tradelink.cryptocurrency.repository.HoldingRepository;
import site.tradelink.tradelink.oauth2.common.principal.CustomOAuth2User;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/auth/holdings")
@RequiredArgsConstructor
public class HoldingAuthController {

    private final HoldingRepository holdingRepository;

    /**
     * 내 보유 주식 목록
     */
    @GetMapping
    public ApiResponse<List<HoldingDto>> getMyHoldings(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long memberSeq = customOAuth2User.getMemberSeq();
        return ApiResponse.ok(
                holdingRepository.findByMemberSeq(memberSeq)
                        .stream()
                        .map(HoldingDto::from)
                        .toList()
        );
    }

    /**
     * 단일 종목 보유 정보
     */
    @GetMapping("/{ticker}")
    public ApiResponse<HoldingDto> getHolding(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable String ticker) {
        Long memberSeq = customOAuth2User.getMemberSeq();
        return ApiResponse.ok(
                holdingRepository.findByMemberSeqAndTicker(memberSeq, ticker)
                        .map(HoldingDto::from)
                        .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 종목: " + ticker))
        );
    }
}
