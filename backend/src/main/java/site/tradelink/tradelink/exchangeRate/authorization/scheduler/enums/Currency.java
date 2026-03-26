package site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 통화 코드
 */
@Getter
@RequiredArgsConstructor
public enum Currency {

    // 미국 달러
    USD("0000001", "미국 달러", "USD"),

    // 일본 엔
    JPY("0000002", "일본 엔화", "JPY"),

    // 유로
    EUR("0000003", "유로", "EUR"),

    // 영국 파운드
    GBP("0000012", "영국 파운드", "GBP"),

    // 중국 위안
    CNY("0000053", "중국 위안화", "CNY");

    private final String itemCode; // 한국은행 API ITEM_CODE1
    private final String koreanName; // 한글명
    private final String currencyCode;
}
