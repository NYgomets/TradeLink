package site.tradelink.tradelink.cryptocurrency.disruptor;

import lombok.Getter;
import lombok.Setter;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.enums.OrderStatus;
import site.tradelink.tradelink.supports.enums.ErrorCode;

@Getter
@Setter
public class OrderEventModel {
    // 주문 인입 데이터 (Input)
    private long memberSeq;
    private String ticker;
    private OrderSide side;
    private double quantity;
    private long reservedAmount;

    // 매칭 결과 데이터 (Output - 매칭 엔진이 덮어쓸 영역)
    private OrderStatus status;
    private double executedQuantity;
    private long executedPrice;
    private ErrorCode errorCode;

    // 프로듀서(OrderPlaceService)가 최초 진입 시 호출
    public void assignInput(long memberSeq, String ticker, OrderSide side, double quantity, long reservedAmount) {
        this.memberSeq = memberSeq;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.reservedAmount = reservedAmount;

        // 초기 상태화
        this.status = OrderStatus.PENDING;
        this.executedQuantity = 0.0;
        this.executedPrice = 0L;
        this.errorCode = null;
    }

    public void clear() {
        this.memberSeq = 0L;
        this.ticker = null;
        this.side = null;
        this.quantity = 0.0;
        this.reservedAmount = 0L;

        this.status = null;
        this.executedQuantity = 0.0;
        this.executedPrice = 0L;
        this.errorCode = null;
    }
}
