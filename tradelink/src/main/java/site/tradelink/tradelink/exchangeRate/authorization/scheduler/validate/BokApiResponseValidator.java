package site.tradelink.tradelink.exchangeRate.authorization.scheduler.validate;

import org.springframework.stereotype.Component;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.dto.BokApiExchangeRateDto;

/**
 * 한국은행 API 응답 검증
 * 추후 error 작업 진행
 */
@Component
public class BokApiResponseValidator {

    /**
     * 응답 유효성 검증
     */
    public void validate (BokApiExchangeRateDto.BokApiExchangeRateResponse response) {
        if (response == null) {
            throw new IllegalStateException("BOK API response is null");
        }

        // 실패 응답 처리
        if (response.getResult() != null) {
            String codeValue = response.getResult().getCode();
            String message = response.getResult().getMessage();

            BokApiErrorCode code = BokApiErrorCode.from(codeValue);

            if (code == null) {
                throw new IllegalStateException("Unknown BOK API result: " + codeValue);
            }

            switch (code) {
                // 데이터 없음
                case INFO_200 -> throw new IllegalStateException("No exchange rate data");

                // 인증 문제
                case INFO_100 -> throw new IllegalStateException("Invalid BOK API auth key");

                // 요청 파라미터 문제
                case ERROR_100, ERROR_101, ERROR_200, ERROR_300, ERROR_301 ->
                        throw new IllegalStateException("Invalid BOK API request: " + message);

                // 재시도 대상
                case ERROR_400, ERROR_602 ->
                        throw new IllegalStateException("Temporary BOK API issue: " + message);

                // 외부 시스템 장애
                case ERROR_500, ERROR_600, ERROR_601 ->
                        throw new IllegalStateException("BOK API server error: " + message);
            }
        }
    }
}
