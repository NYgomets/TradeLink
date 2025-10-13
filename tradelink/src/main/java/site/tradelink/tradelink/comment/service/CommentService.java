package site.tradelink.tradelink.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.comment.request.CommentCreateDto;
import site.tradelink.tradelink.comment.request.CommentUpdateDto;
import site.tradelink.tradelink.comment.response.CommentResponseDto;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentTransactionalService transactionalService;

    public Long createComment(Long postSeq, Long memberSeq, CommentCreateDto request) {
        return transactionalService.createComment(postSeq, memberSeq, request);
    }

    public void updateComment(Long commentSeq, Long memberSeq, CommentUpdateDto request) {
        transactionalService.updateComment(commentSeq, memberSeq, request);
    }

    public void deleteComment(Long commentSeq, Long memberSeq) {
        transactionalService.deleteComment(commentSeq, memberSeq);
    }

    public List<CommentResponseDto>
}
