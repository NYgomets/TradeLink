package site.tradelink.tradelink.exchangeRate.authorization.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/**
 * 한국은행 Open API 환율 응답 DTO
 */
public class BokApiExchangeRateDto {

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BokApiExchangeRateResponse {

        @JsonProperty("StatisticSearch")
        private StatisticSearch statisticSearch;

        @JsonProperty("RESULT")
        private Result result;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatisticSearch {

        @JsonProperty("list_total_count")
        private Integer listTotalCount;

        @JsonProperty("row")
        private List<Row> rows;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {

        @JsonProperty("STAT_CODE")
        private String statCode;

        @JsonProperty("STAT_NAME")
        private String statName;

        @JsonProperty("ITEM_CODE1")
        private String itemCode1;

        @JsonProperty("ITEM_NAME1")
        private String itemName1;

        @JsonProperty("DATA_VALUE")
        private String dataValue;

        @JsonProperty("TIME")
        private String time;

        @JsonProperty("UNIT_NAME")
        private String unitName;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("CODE")
        private String code;

        @JsonProperty("MESSAGE")
        private String message;
    }
}
