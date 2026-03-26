package site.tradelink.tradelink.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.comment.entity.Comment;
import site.tradelink.tradelink.comment.request.CommentCreateDto;
import site.tradelink.tradelink.comment.request.CommentUpdateDto;
import site.tradelink.tradelink.comment.response.CommentResponseDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<CommentResponseDto> getCommentsForPost(Long postSeq) {
        List<Comment> comments = transactionalService.getCommentsByPostSeq(postSeq);

        LinkedHashMap<Long, CommentResponseDto> dtoMap = comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toMap(
                        CommentResponseDto::commentSeq,
                        dto -> dto,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<CommentResponseDto> rootComments = new ArrayList<>();

        comments.forEach(comment -> {
            CommentResponseDto currentDto = dtoMap.get(comment.getSeq());
            if (comment.getParent() != null) {
                CommentResponseDto parentDto = dtoMap.get(comment.getParent().getSeq());
                if (parentDto != null) {
                    parentDto.replies().add(currentDto);
                }
            } else {
                rootComments.add(currentDto);
            }
        });

        return Collections.unmodifiableList(rootComments);
    }
}
