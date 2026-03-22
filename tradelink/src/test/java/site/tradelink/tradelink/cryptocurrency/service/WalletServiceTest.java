package site.tradelink.tradelink.cryptocurrency.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.cryptocurrency.repository.WalletRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService 단위 테스트")
class WalletServiceTest {

    @InjectMocks WalletService walletService;
    @Mock WalletRepository walletRepository;

    private static final Long MEMBER_SEQ = 1L;

    @Nested
    @DisplayName("reserve - 예약 차감")
    class Reserve {

        @Test
        @DisplayName("잔고 충분 시 예약 차감 성공")
        void success() {
            when(walletRepository.reserve(MEMBER_SEQ, 1_000_000L)).thenReturn(1);

            assertThatCode(() -> walletService.reserve(MEMBER_SEQ, 1_000_000L))
                    .doesNotThrowAnyException();

            verify(walletRepository).reserve(MEMBER_SEQ, 1_000_000L);
        }

        @Test
        @DisplayName("잔고 부족 시 (updated=0) 예외 발생")
        void insufficient() {
            when(walletRepository.reserve(MEMBER_SEQ, 1_000_000L)).thenReturn(0);

            assertThatThrownBy(() -> walletService.reserve(MEMBER_SEQ, 1_000_000L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문 가능 금액이 부족");
        }
    }

    @Nested
    @DisplayName("confirmBuy - 매수 체결 확정")
    class ConfirmBuy {

        @Test
        @DisplayName("정상 체결 시 confirmBuy 쿼리 호출")
        void success() {
            long reserved = 1_000_000L;
            long exec     =   900_000L;
            long refund   =   100_000L;

            walletService.confirmBuy(MEMBER_SEQ, reserved, exec);

            verify(walletRepository).confirmBuy(MEMBER_SEQ, exec, refund);
        }

        @Test
        @DisplayName("슬리피지 초과 시 예외 발생, confirmBuy 쿼리 미호출")
        void slippage() {
            long reserved = 1_000_000L;
            long exec     = 1_100_000L;

            assertThatThrownBy(() -> walletService.confirmBuy(MEMBER_SEQ, reserved, exec))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("체결가 상승으로 잔고 부족");

            verify(walletRepository, never()).confirmBuy(any(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("정확히 예약금만큼 체결 시 refund=0 으로 confirmBuy 호출")
        void exactMatch() {
            long amount = 1_000_000L;

            walletService.confirmBuy(MEMBER_SEQ, amount, amount);

            verify(walletRepository).confirmBuy(MEMBER_SEQ, amount, 0L);
        }
    }

    @Nested
    @DisplayName("confirmSell - 매도 체결 확정")
    class ConfirmSell {

        @Test
        @DisplayName("매도 체결 시 confirmSell 쿼리 호출")
        void success() {
            walletService.confirmSell(MEMBER_SEQ, 5_000_000L);

            verify(walletRepository).confirmSell(MEMBER_SEQ, 5_000_000L);
        }
    }

    @Nested
    @DisplayName("deposit - 입금")
    class Deposit {

        @Test
        @DisplayName("정상 입금 시 deposit 쿼리 호출")
        void success() {
            walletService.deposit(MEMBER_SEQ, 10_000_000L);

            verify(walletRepository).deposit(MEMBER_SEQ, 10_000_000L);
        }

        @Test
        @DisplayName("1억 초과 입금 시 예외 발생")
        void exceedLimit() {
            assertThatThrownBy(() -> walletService.deposit(MEMBER_SEQ, 100_000_001L))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(walletRepository, never()).deposit(any(), anyLong());
        }

        @Test
        @DisplayName("0원 입금 시 예외 발생")
        void zero() {
            assertThatThrownBy(() -> walletService.deposit(MEMBER_SEQ, 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}