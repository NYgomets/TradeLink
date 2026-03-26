package site.tradelink.tradelink.cryptocurrency.dto;

public record TotalAssetDto(
        Long balance,           // 현금 총 잔액
        Long availableBalance,  // 주문 가능 금액
        Long holdingValue,      // 보유 종목 평가액
        Long totalAsset         // 총 자산 = balance + holdingValue
) {
}
