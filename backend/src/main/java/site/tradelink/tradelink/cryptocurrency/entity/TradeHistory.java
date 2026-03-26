package site.tradelink.tradelink.cryptocurrency.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.supports.entity.BaseEntity;

import java.time.LocalDateTime;

// 체결 내역
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = @Index(name = "idx_trade_member", columnList = "memberSeq, createTime")
)
public class TradeHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private Long memberSeq;

    private String ticker;

    private String name;

    // BUT | SELL
    private OrderSide side;

    private Double quantity;

    private Long execPrice;

    private Double totalAmount;

    public static TradeHistory of(Long memberSeq, OrderEvent event, long execPrice, String name) {
        return TradeHistory.builder()
                .memberSeq   (memberSeq)
                .ticker     (event.getTicker())
                .name       (name)
                .side       (event.getSide())
                .quantity   (event.getQuantity())
                .execPrice  (execPrice)
                .totalAmount(execPrice * event.getQuantity())
                .build();
    }
}
