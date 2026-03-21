package site.tradelink.tradelink.cryptocurrency.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.supports.entity.BaseEntity;

/**
 * 주문 이벤트 (Append-Only)
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = {
                @Index(name = "idx_order_event_ticker", columnList = "ticker"),
        }
)
public class OrderEvent extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private String ticker;

    private Long memberSeq;

    // BUY | SELL
    @Enumerated(EnumType.STRING)
    private OrderSide side;

    private Double quantity;

    private Long reservedPrice;

    public static OrderEvent create(Long memberSeq, String ticker, OrderSide side, Double quantity, Long reservedPrice) {
        return OrderEvent.builder()
                .memberSeq(memberSeq)
                .ticker(ticker)
                .side(side)
                .quantity(quantity)
                .reservedPrice(reservedPrice)
                .build();
    }
}
