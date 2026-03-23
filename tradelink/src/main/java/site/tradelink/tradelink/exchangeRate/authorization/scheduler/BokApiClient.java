package site.tradelink.tradelink.exchangeRate.authorization.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.dto.BokApiExchangeRateDto;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums.StatisticCode;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.validate.BokApiResponseValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 한국은행 Open API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BokApiClient {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int MAX_FALLBACK_DAYS = 7;
    private final RestClient bokRestClient;
    private final BokApiResponseValidator validator;

    @Value("${bok.api.auth-key}")
    private String authKey;

    /**
     * 최신 환율 조회
     * 당일 데이터 없으면 최대 7일 전까지 폴백
     */
    public BokApiExchangeRateDto.Row getLatestExchangeRate(Currency currency) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i <= MAX_FALLBACK_DAYS; i++) {
            LocalDate targetDate = today.minusDays(i);

            BokApiExchangeRateDto.BokApiExchangeRateResponse response = fetchData(
                    StatisticCode.EXCHANGE_RATE,
                    currency.getItemCode(),
                    targetDate,
                    targetDate
            );

            if (hasData(response)) {
                if (i > 0) {
                    log.info("[BOK] {} 당일 데이터 없음 → {}일 전({}) 데이터 사용",
                            currency.name(), i, targetDate);
                }
                validator.validate(response);
                return response.getStatisticSearch().getRows().getFirst();
            }
        }

        // 7일 이내 데이터가 전혀 없는 경우 (거의 발생하지 않음)
        throw new RuntimeException(
                "[BOK] " + currency.name() + " 최근 " + MAX_FALLBACK_DAYS + "일 이내 데이터 없음"
        );
    }

    /**
     * 응답에 실제 데이터가 있는지 확인
     */
    private boolean hasData(BokApiExchangeRateDto.BokApiExchangeRateResponse response) {
        return response != null
                && response.getStatisticSearch() != null
                && response.getStatisticSearch().getRows() != null
                && !response.getStatisticSearch().getRows().isEmpty()
                && response.getStatisticSearch().getRows().getFirst().getDataValue() != null
                && !response.getStatisticSearch().getRows().getFirst().getDataValue().isBlank();
    }

    /**
     * 데이터 조회
     */
    private BokApiExchangeRateDto.BokApiExchangeRateResponse fetchData(StatisticCode statCode, String itemCode, LocalDate startDate, LocalDate endDate) {
        String uri = buildUri(statCode, itemCode, startDate, endDate);

        try {
            return bokRestClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(BokApiExchangeRateDto.BokApiExchangeRateResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch BOK API data: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * URI 생성
     */
    private String buildUri(StatisticCode statCode, String itemCode, LocalDate startDate, LocalDate endDate) {
        return String.format(
                "/StatisticSearch/%s/json/kr/1/1/%s/%s/%s/%s/%s",
                authKey,
                statCode.getCode(),
                statCode.getCycle(),
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                itemCode
        );
    }
}
