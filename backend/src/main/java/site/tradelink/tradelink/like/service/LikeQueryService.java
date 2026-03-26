package site.tradelink.tradelink.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.response.LikeStatusResponseDto;

@Service
@RequiredArgsConstructor
public class LikeQueryService {

    private final LikeStatusRepository likeStatusRepository;
    private final PostStatsRepository postStatsRepository;

    /**
     * [게시글 상세 조회용]
     * 1. 내가 좋아요 눌렀는지 -> LikeStatus 행 존재 여부
     * 2. 총 좋아요 개수
     */
    @Transactional(readOnly = true)
    public LikeStatusResponseDto getLikeStatus(Long memberSeq, Long postSeq) {
        boolean isLiked = false;

        if (memberSeq != null) {
            isLiked = likeStatusRepository.findByMemberSeqAndPostSeq(memberSeq, postSeq).isPresent();
        }

        Long likeCount = postStatsRepository.findByPostSeq(postSeq)
                .map(PostStats::getLikeCount)
                .orElse(0L);

        return LikeStatusResponseDto.of(postSeq, isLiked, likeCount);
    }
}
