package site.tradelink.tradelink.supports.request;

import jakarta.validation.constraints.NotNull;
import site.tradelink.tradelink.supports.enums.ErrorCode;

public record ExceptionDto (
        @NotNull
        Integer code,

        @NotNull
        String message
) {
    public static ExceptionDto of(ErrorCode errorCode) {
        return new ExceptionDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
