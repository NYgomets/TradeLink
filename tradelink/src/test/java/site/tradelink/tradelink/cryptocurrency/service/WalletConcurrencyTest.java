package site.tradelink.tradelink.cryptocurrency.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import site.tradelink.tradelink.cryptocurrency.entity.Wallet;
import site.tradelink.tradelink.cryptocurrency.repository.WalletRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Wallet 동시성 통합 테스트")
class WalletConcurrencyTest {

    @Autowired WalletService    walletService;
    @Autowired WalletRepository walletRepository;

    private static final Long MEMBER_SEQ    = 100L;
    private static final long INITIAL       = 200_000_000L;
    private static final long RESERVE_AMOUNT = 1_000_000L;

    @BeforeEach
    void setUp() {
        walletRepository.findByMemberSeq(MEMBER_SEQ)
                .ifPresent(walletRepository::delete);
        walletRepository.save(Wallet.create(MEMBER_SEQ));
    }

    @Test
    @DisplayName("동시 10건 예약 차감 시 정확히 성공한 건수만큼만 차감된다")
    void concurrentReserve() throws InterruptedException {
        int threadCount = 10;
        long maxSuccess = INITIAL / RESERVE_AMOUNT; // 200번 성공 가능

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    walletService.reserve(MEMBER_SEQ, RESERVE_AMOUNT);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Wallet wallet = walletRepository.findByMemberSeq(MEMBER_SEQ).orElseThrow();

        // 성공 건수만큼 정확히 차감됐는지 확인
        assertThat(wallet.getAvailableBalance())
                .isEqualTo(INITIAL - (long) successCount.get() * RESERVE_AMOUNT);

        // 성공 + 실패 = 전체 요청 수
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("잔고 초과 동시 요청 시 잔고 이하로만 차감된다 (음수 불가)")
    void concurrentReserveExceedBalance() throws InterruptedException {
        int  threadCount    = 300; // 2억 / 1백만 = 200번만 성공 가능
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    walletService.reserve(MEMBER_SEQ, RESERVE_AMOUNT);
                    successCount.incrementAndGet();
                } catch (IllegalStateException ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Wallet wallet = walletRepository.findByMemberSeq(MEMBER_SEQ).orElseThrow();

        // availableBalance 는 절대 음수가 되면 안 됨
        assertThat(wallet.getAvailableBalance()).isGreaterThanOrEqualTo(0L);
        // 최대 200건만 성공
        assertThat(successCount.get()).isLessThanOrEqualTo(200);
        // 실제 차감액 = 성공 건수 × 예약금
        assertThat(wallet.getAvailableBalance())
                .isEqualTo(INITIAL - (long) successCount.get() * RESERVE_AMOUNT);
    }
}
