package site.tradelink.tradelink.stock.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
public class Stock {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long seq;

    // 종목 코드
    private String stockName;
    // 종목 이름
    private String stockCode;

    // 현재가
    private int currentPrice;

    // 고가
    private int highestPrice;

    // 저가
    private int lowestPrice;

    // 전일 종가
    private int previousClosePrice;

    // 전일비
    private int priceChange;

    // 등락률
    private double priceChangePercent;

    // 거래량
    private int volume;

    // 거래대금
    private long tradingVolume;

    // 시가 총액
    private long marketCap;

    // 52주 최고가
    private int totalHighestPrice;

    // 52주 최저가
    private int totalLowestPrice;


}
