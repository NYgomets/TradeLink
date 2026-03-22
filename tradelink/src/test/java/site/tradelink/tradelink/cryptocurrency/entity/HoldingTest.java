package site.tradelink.tradelink.cryptocurrency.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Holding 도메인 단위 테스트")
class HoldingTest {

    Holding holding;

    @BeforeEach
    void setUp() {
        holding = Holding.create(1L, "BTC", "비트코인");
    }

    @Nested
    @DisplayName("buy - 매수 평균가 계산")
    class Buy {

        @Test
        @DisplayName("첫 매수 시 평균가 = 체결가")
        void firstBuy() {
            holding.buy(100_000_000L, 0.5);

            assertThat(holding.getQuantity()).isEqualTo(0.5);
            assertThat(holding.getAvgPrice()).isEqualTo(100_000_000L);
        }

        @Test
        @DisplayName("추가 매수 시 평균가가 올바르게 재계산된다")
        void additionalBuy() {
            // 1차 매수: 1개 @ 100만
            holding.buy(1_000_000L, 1.0);
            // 2차 매수: 1개 @ 200만
            holding.buy(2_000_000L, 1.0);

            // 평균 = (100만 + 200만) / 2 = 150만
            assertThat(holding.getQuantity()).isEqualTo(2.0);
            assertThat(holding.getAvgPrice()).isEqualTo(1_500_000L);
        }

        @Test
        @DisplayName("소수점 수량 매수 시 평균가가 올바르게 계산된다")
        void decimalBuy() {
            holding.buy(100_000_000L, 0.1);
            holding.buy(110_000_000L, 0.1);

            assertThat(holding.getQuantity()).isEqualTo(0.2);
            // (100,000,000 * 0.1 + 110,000,000 * 0.1) / 0.2 = 105,000,000
            assertThat(holding.getAvgPrice()).isEqualTo(105_000_000L);
        }
    }

    @Nested
    @DisplayName("sell - 매도 수량 차감")
    class Sell {

        @BeforeEach
        void buyFirst() {
            holding.buy(100_000_000L, 1.0);
        }

        @Test
        @DisplayName("정상 매도 시 수량이 차감된다")
        void sell() {
            holding.sell(0.3);

            assertThat(holding.getQuantity()).isEqualTo(0.7);
        }

        @Test
        @DisplayName("전량 매도 시 수량이 0이 된다")
        void sellAll() {
            holding.sell(1.0);

            assertThat(holding.getQuantity()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("보유 수량 초과 매도 시 예외 발생")
        void sellExceed() {
            assertThatThrownBy(() -> holding.sell(1.5))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("보유 수량 부족");
        }

        @Test
        @DisplayName("보유량 없이 매도 시 예외 발생")
        void sellWithoutHolding() {
            Holding empty = Holding.create(1L, "ETH", "이더리움");

            assertThatThrownBy(() -> empty.sell(0.1))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}