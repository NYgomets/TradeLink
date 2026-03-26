package site.tradelink.tradelink.cryptocurrency.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;

public record OrderRequestDto(

        @NotBlank
        String ticker,

        @NotNull
        OrderSide side,

        @NotNull
        @DecimalMin(value = "0.00000001", message = "최소 주문 수량은 0.00000001 입니다")
        Double quantity
) {}
