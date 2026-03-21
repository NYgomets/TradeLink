package site.tradelink.tradelink.cryptocurrency.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.cryptocurrency.entity.Wallet;
import site.tradelink.tradelink.cryptocurrency.repository.WalletRepository;

/**
 * 지갑 서비스
 * 예약 차감: WalletRepository.reserve() 원자적 UPDATE 쿼리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    // 예약 차감 (placeOrder 시점)
    @Transactional
    public void reserve(Long memberSeq, long amount) {
        int updated = walletRepository.reserve(memberSeq, amount);
        if (updated == 0) {
            throw new IllegalStateException(
                    "주문 가능 금액이 부족합니다. (요청=" + amount + "원)");
        }
    }

    // 예약 취소
    @Transactional
    public void cancelReservation(Long memberSeq, long amount) {
        walletRepository.cancelReservation(memberSeq, amount);
    }

    // 체결 확정 (MatchingEngine 시점)
    @Transactional
    public void confirmBuy(Long memberSeq, long reservedAmount, long execAmount) {
        if (execAmount > reservedAmount) {
            throw new IllegalStateException(
                    "체결가 상승으로 잔고 부족 (예약=" + reservedAmount + ", 체결=" + execAmount + ")");
        }
        long refund = reservedAmount - execAmount;
        walletRepository.confirmBuy(memberSeq, execAmount, refund);
    }

    @Transactional
    public void confirmSell(Long memberSeq, long execAmount) {
        walletRepository.confirmSell(memberSeq, execAmount);
    }

    // 입금
    @Transactional
    public void deposit(Long memberSeq, long amount) {
        if (amount <= 0 || amount > 100_000_000L) {
            throw new IllegalArgumentException("입금액은 1원 이상 1억원 이하만 가능합니다");
        }
        walletRepository.deposit(memberSeq, amount);
    }

    // 조회
    @Transactional(readOnly = true)
    public Wallet getWallet(Long memberSeq) {
        return walletRepository.findByMemberSeq(memberSeq)
                .orElseThrow(() -> new IllegalStateException("지갑 없음: " + memberSeq));
    }
}
