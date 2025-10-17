package site.tradelink.tradelink.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.comment.entity.Comment;
import site.tradelink.tradelink.comment.repository.CommentRepository;
import site.tradelink.tradelink.comment.request.CommentCreateDto;
import site.tradelink.tradelink.comment.request.CommentUpdateDto;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentTransactionalService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createComment(Long postSeq, Long memberSeq, CommentCreateDto request) {
        // 추후 Error 작업 추가
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment actualParentComment = null;
        if (request.getParentCommentSeq() != null) {
            Comment clickedParentComment = commentRepository.findByIdWithParent(request.getParentCommentSeq())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

            if (clickedParentComment.getDepth() == 0) {
                actualParentComment = clickedParentComment;
            } else {
                actualParentComment = clickedParentComment.getParent();
            }
        }

        Comment comment = Comment.createComment(request.getContent(), post, member, actualParentComment);
        return commentRepository.save(comment).getSeq();
    }

    @Transactional
    public void updateComment(Long commentSeq, Long memberSeq, CommentUpdateDto request) {
        // 추후 Error 작업 추가
        Comment comment = commentRepository.findActiveCommentBySeqAndMemberSeq(commentSeq, memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없거나 수정 권한이 없습니다."));

        comment.update(request.getContent());
    }

    @Transactional
    public void deleteComment(Long commentSeq, Long memberSeq) {
        // 추후 Error 작업 추가
        Comment comment = commentRepository.findActiveCommentBySeqAndMemberSeq(commentSeq, memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없거나 삭제 권한이 없습니다."));

        comment.softDelete();
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostSeq(Long postSeq) {
        return commentRepository.findAllByPostSeq(postSeq);
    }
}
