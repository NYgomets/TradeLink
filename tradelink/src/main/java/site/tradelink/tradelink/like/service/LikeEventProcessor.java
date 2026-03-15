package site.tradelink.tradelink.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.LikeStatus;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeEventProcessor {

    private final LikeStatusRepository likeStatusRepository;

    @Transactional
    public boolean processSingleLikeStatus(LikePostEvent event) {
        boolean isLikedAction = event.getActionType() == ActionType.LIKE;

        Optional<LikeStatus> optional = likeStatusRepository.findByMemberSeqAndPostSeq(event.getMemberSeq(), event.getPostSeq());

        if (optional.isEmpty()) {
            likeStatusRepository.save(LikeStatus.builder()
                    .memberSeq(event.getMemberSeq())
                    .postSeq(event.getPostSeq())
                    .isLiked(isLikedAction)
                    .build());

            return isLikedAction;
        }

        LikeStatus likeStatus = optional.get();

        if (likeStatus.getIsLiked() == isLikedAction) {
            return false;
        }

        likeStatus.updateLikeStatus(isLikedAction);
        return true;
    }
}
