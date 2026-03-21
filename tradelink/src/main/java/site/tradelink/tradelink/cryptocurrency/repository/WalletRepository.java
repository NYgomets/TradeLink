package site.tradelink.tradelink.cryptocurrency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.cryptocurrency.entity.Wallet;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByMemberSeq(Long memberSeq);

    /**
     * 원자적 예약 차감
     * DB 레벨에서 조회 + 검증 + 차감을 단일 쿼리로 처리
     * available_balance > = amount 조건 불만족 시 0 반환 -> 잔고 부족
     */
    @Modifying
    @Query("""
            UPDATE Wallet w
            SET w.availableBalance = w.availableBalance - :amount
            WHERE w.memberSeq = :memberSeq
              AND w.availableBalance >= :amount
            """)
    int reserve(@Param("memberSeq") Long memberSeq, @Param("amount") long amount);

    /**
     * 원자적 예약 취소 (주문 실패 시 환불)
     */
    @Modifying
    @Query("""
            UPDATE Wallet w
            SET w.availableBalance = w.availableBalance + :amount
            WHERE w.memberSeq = :memberSeq
            """)
    void cancelReservation(@Param("memberSeq") Long memberSeq, @Param("amount") long amount);

    /**
     * 원자적 매수 체결 확정
     * balance 확정 차감 + 차액(refund) availableBalance 환불
     * refund = reservedAmount - execAmount (더 싸게 체결된 경우 > 0)
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Wallet w
            SET w.balance = w.balance - :execAmount,
                w.availableBalance = w.availableBalance + :refund
            WHERE w.memberSeq = :memberSeq
            """)
    void confirmBuy(@Param("memberSeq") Long memberSeq,
                    @Param("execAmount") long execAmount,
                    @Param("refund") long refund);


    /**
     * 원자적 매도 체결 확정
     * balance + availableBalance 모두 증가
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Wallet w
            SET w.balance = w.balance + :execAmount,
                w.availableBalance = w.availableBalance + :execAmount
            WHERE w.memberSeq = :memberSeq
            """)
    void confirmSell(@Param("memberSeq") Long memberSeq, @Param("execAmount") long execAmount);

    /**
     * 원자적 입금
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Wallet w
            SET w.balance = w.balance + :amount,
                w.availableBalance = w.availableBalance + :amount
            WHERE w.memberSeq = :memberSeq
            """)
    void deposit(@Param("memberSeq") Long memberSeq, @Param("amount") long amount);

}
