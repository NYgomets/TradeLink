package site.tradelink.tradelink.cryptocurrency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.cryptocurrency.dto.TotalAssetDto;
import site.tradelink.tradelink.cryptocurrency.dto.WalletDto;
import site.tradelink.tradelink.cryptocurrency.entity.Holding;
import site.tradelink.tradelink.cryptocurrency.entity.Wallet;
import site.tradelink.tradelink.cryptocurrency.inMemory.StockPriceCache;
import site.tradelink.tradelink.cryptocurrency.repository.HoldingRepository;
import site.tradelink.tradelink.cryptocurrency.service.WalletService;
import site.tradelink.tradelink.oauth2.common.principal.CustomOAuth2User;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/auth/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final HoldingRepository holdingRepository;
    private final StockPriceCache priceCache;

    /**
     * 지갑 조회 (현금 잔고)
     */
    @GetMapping
    public ApiResponse<WalletDto> getWallet(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        Long memberSeq = customOAuth2User.getMemberSeq();
        Wallet wallet = walletService.getWallet(memberSeq);
        return ApiResponse.ok(WalletDto.from(wallet));
    }

    /**
     * 총 자산 조회
     * 총 자산 = 현금(availableBalance) + 보유 종목 평가액 합계
     */
    @GetMapping("/total-asset")
    public ApiResponse<TotalAssetDto> getTotalAsset(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        Long memberSeq = customOAuth2User.getMemberSeq();
        Wallet wallet = walletService.getWallet(memberSeq);
        List<Holding> holdings = holdingRepository.findByMemberSeq(memberSeq);

        // 보유 종목 평가액 계산
        long holdingValue = holdings.stream()
                .mapToLong(holding ->
                        priceCache.findPrice(holding.getTicker())
                                .map(price -> (long) (price.price() * holding.getQuantity()))
                                .orElse(0L)
                )
                .sum();

        long totalAsset = wallet.getBalance() + holdingValue;

        return ApiResponse.ok(new TotalAssetDto(
                wallet.getBalance(),
                wallet.getAvailableBalance(),
                holdingValue,
                totalAsset
        ));
    }

    /**
     * 입금
     * 최대 1억원 단위로 제한
     */
    @PostMapping("/deposit")
    public ApiResponse<WalletDto> deposit(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @RequestParam long amount) {

        Long memberSeq = customOAuth2User.getMemberSeq();
        if (amount <= 0 || amount > 100_000_000L) {
            throw new IllegalArgumentException("입금액은 1원 이상 1억원 이하만 가능합니다");
        }

        walletService.deposit(memberSeq, amount);
        return ApiResponse.ok(WalletDto.from(walletService.getWallet(memberSeq)));
    }
}
