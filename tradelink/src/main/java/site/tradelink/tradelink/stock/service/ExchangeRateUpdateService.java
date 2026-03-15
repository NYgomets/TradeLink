package site.tradelink.tradelink.stock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.stock.authorization.scheduler.BokApiClient;
import site.tradelink.tradelink.stock.authorization.scheduler.dto.BokApiExchangeRateDto;
import site.tradelink.tradelink.stock.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.stock.entity.CurrentExchangeRate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 환율 업데이트 서비스 (외부 API 호출 담당)
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateUpdateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BokApiClient bokApiClient;
    private final ExchangeRateDataService dataService;

    /**
     * 특정 통화 업데이트
     */
    public CurrentExchangeRate updateCurrency(Currency currency) {

        try {
            // 1. 외부 API 호출
            BokApiExchangeRateDto.Row latest = bokApiClient.getLatestExchangeRate(currency);

            // 2. 데이터 파싱
            Double rate = Double.parseDouble(latest.getDataValue());
            LocalDateTime dateTime = parseDateTime(latest.getTime());

            // 3. DB 저장
            return dataService.saveExchangeRateData(currency, rate, dateTime);
        } catch (Exception e) {
            throw new RuntimeException("Currency update failed: " + currency.name(), e);
        }

    }

    /**
     * 날짜 파싱
     */
    private LocalDateTime parseDateTime(String time) {
        try {
            LocalDate date = LocalDate.parse(time, DATE_FORMATTER);
            return date.atTime(LocalTime.now());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
