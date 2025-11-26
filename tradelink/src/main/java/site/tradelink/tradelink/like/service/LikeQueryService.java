package site.tradelink.tradelink.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.entity.LikeStatus;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.response.LikeStatusResponseDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeQueryService {
    private final LikeStatusRepository likeStatusRepository;
    private final PostStatsRepository postStatsRepository;

    /**
     * [게시글 상세 조회용]
     * 게시글 안으로 들어왔을 때만 호출됩니다.
     * 1. 내가 좋아요 눌렀는지 (버튼 색칠용)
     * 2. 총 좋아요 개수 (숫자 표기용)
     */
    public LikeStatusResponseDto getLikeStatus(Long memberSeq, Long postSeq) {
        Boolean isLiked = likeStatusRepository.findByMemberSeqAndPostSeq(memberSeq, postSeq)
                .map(LikeStatus::getIsLiked)
                .orElse(false);

        Long likeCount = getPostLikeCount(postSeq);
        return LikeStatusResponseDto.of(postSeq, isLiked, likeCount);
    }

    private Long getPostLikeCount(Long postSeq) {
        return postStatsRepository.findByPostSeq(postSeq)
                .map(PostStats::getLikeCount)
                .orElse(0L);
    }

    // 내가 좋아요한 게시글 목록만 제공
    public Page<Long> getMyLikedPostSeqs(Long memberSeq, Pageable pageable) {
        return likeStatusRepository.findLikedPostSeqs(memberSeq, pageable);
    }
}
