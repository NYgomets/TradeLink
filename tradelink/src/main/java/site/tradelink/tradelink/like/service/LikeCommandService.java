package site.tradelink.tradelink.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.repository.LikePostEventRepository;
import site.tradelink.tradelink.like.request.LikePostDto;
import site.tradelink.tradelink.like.response.LikePostResponseDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeCommandService {
    private final LikePostEventRepository  likePostEventRepository;

    @Transactional
    public LikePostResponseDto processBatchLikePostEvents(Long memberSeq, LikePostDto request) {
        List<LikePostEvent> events = request.getActions().stream()
                .map(action -> LikePostEvent.builder()
                        .memberSeq(memberSeq)
                        .postSeq(action.getPostSeq())
                        .actionType(action.getActionType())
                        .build())
                .toList();

        likePostEventRepository.saveAll(events);

        return LikePostResponseDto.accepted(events.size());
    }
}
