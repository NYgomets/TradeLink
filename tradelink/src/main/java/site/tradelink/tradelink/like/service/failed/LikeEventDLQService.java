package site.tradelink.tradelink.like.service.failed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.failed.LikeEventDLQ;
import site.tradelink.tradelink.like.repository.failed.LikeEventDLQRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeEventDLQService {

    private final LikeEventDLQRepository likeEventDLQRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveToDLQ(LikePostEvent event) {
        Optional<LikeEventDLQ> existingDLQ = likeEventDLQRepository.findByOriginalEventSeq(event.getSeq());

        if (existingDLQ.isPresent()) {
            // 이미 DLQ에 있으면 재시도 카운트 증가
            LikeEventDLQ dlq = existingDLQ.get();
            dlq.incrementRetryCount();
        } else {
            LikeEventDLQ dlq = LikeEventDLQ.builder()
                    .originalEventSeq(event.getSeq())
                    .memberSeq(event.getMemberSeq())
                    .postSeq(event.getPostSeq())
                    .actionType(event.getActionType())
                    .retryCount(1)
                    .build();
            likeEventDLQRepository.save(dlq);
        }
    }
}
