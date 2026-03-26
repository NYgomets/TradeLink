package site.tradelink.tradelink.exchangeRate.authorization.scheduler.validate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BokApiErrorCode {

    INFO_100("INFO-100", "Invalid auth key"),
    INFO_200("INFO-200", "No data"),

    ERROR_100("ERROR-100", "Missing required value"),
    ERROR_101("ERROR-101", "Invalid date format"),
    ERROR_200("ERROR-200", "Invalid file type"),
    ERROR_300("ERROR-300", "Missing row count"),
    ERROR_301("ERROR-301", "Invalid row count type"),
    ERROR_400("ERROR-400", "Timeout"),
    ERROR_500("ERROR-500", "Server error"),
    ERROR_600("ERROR-600", "DB connection error"),
    ERROR_601("ERROR-601", "SQL error"),
    ERROR_602("ERROR-602", "Rate limit exceeded");

    private final String code;
    private final String message;

    public static BokApiErrorCode from(String code) {
        for (BokApiErrorCode value : BokApiErrorCode.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }

        return null;
    }
}
