package site.tradelink.tradelink.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikeStatus;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.response.LikePostResponseDto;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeStatusRepository likeStatusRepository;
    private final PostStatsRepository postStatsRepository;

    @Transactional
    public LikePostResponseDto toggleLike(Long memberSeq, Long postSeq, ActionType actionType) {
        boolean isLike = actionType == ActionType.LIKE;

        LikeStatus likeStatus = likeStatusRepository.findByMemberSeqAndPostSeq(memberSeq, postSeq).orElse(null);

        if (likeStatus == null) {
            // 좋아요 기록 없는데 취소 요청 -> 무시
            if (!isLike) {
                return LikePostResponseDto.of(false, getCurrentLikeCount(postSeq));
            }

            // 좋아요면 행 INSERT
            likeStatusRepository.save(LikeStatus.builder()
                    .memberSeq(memberSeq)
                    .postSeq(postSeq)
                    .build()
            );

            applyDelta(postSeq, true);
        } else {
            //좋아요 기록 있는데 좋아요 요청 -> 중복, 무시
            if (isLike) {
                return LikePostResponseDto.of(true, getCurrentLikeCount(postSeq));
            }

            // 취소면 행 DELETE
            likeStatusRepository.delete(likeStatus);

            applyDelta(postSeq, false);
        }

        return LikePostResponseDto.of(isLike, getCurrentLikeCount(postSeq));
    }

    private Long getCurrentLikeCount(Long postSeq) {
        return postStatsRepository.findByPostSeq(postSeq)
                .map(PostStats::getLikeCount)
                .orElse(0L);
    }

    /**
     * DB 레벨 ATOMIC 증감
     * - updated == 0 이면 PostStats 행 자체가 없는 것 (신규 게시글) -> INSERT
     */
    private void applyDelta(Long postSeq, boolean isLike) {

        int updated = isLike ? postStatsRepository.incrementLikeCount(postSeq) : postStatsRepository.decrementLikeCount(postSeq);

        if (updated == 0 && isLike) {
            postStatsRepository.save(PostStats.builder()
                            .postSeq(postSeq)
                            .likeCount(1L)
                            .build()
            );
        }
    }
}
