package site.tradelink.tradelink.cryptocurrency.dto;

import site.tradelink.tradelink.cryptocurrency.entity.Holding;

public record HoldingDto(
        Long   seq,
        String ticker,
        String name,
        Double quantity,
        Double avgPrice
) {
    public static HoldingDto from(Holding e) {
        return new HoldingDto(
                e.getSeq(),
                e.getTicker(),
                e.getName(),
                e.getQuantity(),
                e.getAvgPrice()
        );
    }
}
