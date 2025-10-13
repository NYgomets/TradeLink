package site.tradelink.tradelink.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import site.tradelink.tradelink.common.enums.ErrorCode;
import site.tradelink.tradelink.common.exception.CustomException;
import site.tradelink.tradelink.common.request.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // NotFound 예외
    @ExceptionHandler(value = {NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ApiResponse<?> handleNoFoundException(Exception e) {
        log.warn("Invalid URI request occurred", e);
        return ApiResponse.fail(ErrorCode.NOT_FOUND_END_POINT);
    }

    // Custom 예외
    @ExceptionHandler(value = {CustomException.class})
    public ApiResponse<?> handleCustomException(CustomException e) {
        return ApiResponse.fail(e.getErrorCode());
    }

    // 기본 예외
    @ExceptionHandler(value = {Exception.class})
    public ApiResponse<?> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
