package site.tradelink.tradelink.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import site.tradelink.tradelink.common.enums.ErrorCode;

public record ApiResponse<T> (

    @JsonIgnore
    HttpStatus httpStatus,

    @NotNull
    boolean success,

    @Nullable
    T data,

    @Nullable
    ExceptionDto error
) {
    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    public static <T> ApiResponse<T> fail(final ErrorCode c) {
        return new ApiResponse<>(c.getHttpStatus(), false, null, ExceptionDto.of(c));
    }
}
