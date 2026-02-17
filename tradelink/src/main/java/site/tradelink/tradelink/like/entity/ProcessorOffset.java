package site.tradelink.tradelink.like.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessorOffset extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "processor_offset_seq")
    private Long seq;

    private String processorName;

    private Long lastProcessedEventSeq;

    public void updateOffset(Long lastProcessedEventSeq) {
        this.lastProcessedEventSeq = lastProcessedEventSeq;
    }
}
