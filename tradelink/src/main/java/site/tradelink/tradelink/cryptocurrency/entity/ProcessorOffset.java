package site.tradelink.tradelink.cryptocurrency.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = {
                @Index(name = "idx_processor_offset_ticker", columnList = "ticker")
        }
)
public class ProcessorOffset extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private String ticker;

    private Long lastSeq;

    public void advance (Long lastSeq) {
        this.lastSeq = lastSeq;
    }
}
