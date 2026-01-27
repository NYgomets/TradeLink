package site.tradelink.tradelink.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.LikeStatus;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;

@Service
@RequiredArgsConstructor
public class LikeEventProcessor {

    private final LikeStatusRepository likeStatusRepository;

    @Transactional
    public boolean processSingleLikeStatus(LikePostEvent event) {
        LikeStatus likeStatus = likeStatusRepository.findByMemberSeqAndPostSeq(event.getMemberSeq(), event.getPostSeq())
                .orElseGet(() -> likeStatusRepository.save(LikeStatus.builder()
                        .memberSeq(event.getMemberSeq())
                        .postSeq(event.getPostSeq())
                        .isLiked(false)
                        .build()));

        boolean isLikedAction = event.getActionType() == ActionType.LIKE;

        if (likeStatus.getIsLiked().equals(isLikedAction)) {
            return false;
        }

        likeStatus.updateLikeStatus(isLikedAction);
        return true;
    }
}
