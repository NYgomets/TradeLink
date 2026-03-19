package site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 한국은행 통계표 코드
 */
@Getter
@RequiredArgsConstructor
public enum StatisticCode {

    // 주요국 통화의 대원화환율 (3.1.1.1)
    EXCHANGE_RATE("731Y001", "D");

    private final String code;    // 통계표코드
    private final String cycle;   // 주기 (D: 일별)
}
