package site.tradelink.tradelink.supports.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /**
     * 사용자 지정 Exception
     * code : Custom Code
     * httpStatus : Code of HttpStatus
     * message : Error Message
     */

    // 404 Not Found
    NOT_FOUND_END_POINT(404, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."),
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    // ── 모의투자 ──────────────────────────────────────────────────────────────
    INSUFFICIENT_BALANCE(4001, HttpStatus.BAD_REQUEST, "주문 가능 금액이 부족합니다"),
    INSUFFICIENT_HOLDING(4002, HttpStatus.BAD_REQUEST, "보유 수량이 부족합니다"),
    ORDERBOOK_STALE(4003, HttpStatus.SERVICE_UNAVAILABLE, "호가 데이터를 수신 중입니다. 잠시 후 다시 시도해주세요"),
    WALLET_NOT_FOUND(4004, HttpStatus.NOT_FOUND, "지갑 정보를 찾을 수 없습니다"),
    SLIPPAGE_EXCEEDED(4005, HttpStatus.BAD_REQUEST, "체결가 상승으로 잔고가 부족합니다"),
    HOLDING_NOT_FOUND(4006, HttpStatus.BAD_REQUEST, "보유하지 않은 종목입니다");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
