package site.tradelink.tradelink.supports.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.tradelink.tradelink.supports.enums.ErrorCode;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException{

    private final ErrorCode errorCode;

    public String getMessage() {
        return errorCode.getMessage();
    }
}
