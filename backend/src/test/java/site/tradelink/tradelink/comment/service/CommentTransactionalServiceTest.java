package site.tradelink.tradelink.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.comment.common.enums.CommentStatus;
import site.tradelink.tradelink.comment.entity.Comment;
import site.tradelink.tradelink.comment.repository.CommentRepository;
import site.tradelink.tradelink.comment.request.CommentCreateDto;
import site.tradelink.tradelink.comment.request.CommentUpdateDto;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentTransactionalService 단위 테스트")
class CommentTransactionalServiceTest {

    @InjectMocks
    private CommentTransactionalService commentTransactionalService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member mockMember;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        mockMember = mock(Member.class);
        mockPost = mock(Post.class);

        given(mockMember.getSeq()).willReturn(1L);
        given(mockPost.getSeq()).willReturn(1L);
    }

    // -------------------------------------------------------------------------
    // createComment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("댓글 생성 - createComment")
    class CreateComment {

        private CommentCreateDto mockRequest;

        @BeforeEach
        void setUp() {
            mockRequest = mock(CommentCreateDto.class);
            given(mockRequest.getContent()).willReturn("test content");
        }

        @Test
        @DisplayName("parentCommentSeq가 null이면 루트 댓글로 저장된다")
        void createsRootCommentWhenParentCommentSeqIsNull() {
            // given
            given(mockRequest.getParentCommentSeq()).willReturn(null);
            given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
            given(postRepository.findById(1L)).willReturn(Optional.of(mockPost));

            Comment savedComment = Comment.createComment("test content", mockPost, mockMember, null);
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            commentTransactionalService.createComment(1L, 1L, mockRequest);

            // then
            verify(commentRepository, never()).findByIdWithParent(any());
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("depth 0인 댓글에 답글을 달면 depth 1로 저장된다")
        void createsChildCommentWithDepth1WhenParentIsRootComment() {
            // given
            Comment rootComment = Comment.createComment("root", mockPost, mockMember, null);

            given(mockRequest.getParentCommentSeq()).willReturn(10L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
            given(postRepository.findById(1L)).willReturn(Optional.of(mockPost));
            given(commentRepository.findByIdWithParent(10L)).willReturn(Optional.of(rootComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            commentTransactionalService.createComment(1L, 1L, mockRequest);

            // then
            verify(commentRepository).save(argThat(c -> c.getDepth() == 1));
        }

        @Test
        @DisplayName("depth 1인 댓글에 답글을 달면 실제 부모는 루트 댓글로 조정된다")
        void usesGrandparentAsActualParentWhenClickedCommentIsDepth1() {
            // given
            Comment rootComment = Comment.createComment("root", mockPost, mockMember, null);
            Comment childComment = Comment.createComment("child", mockPost, mockMember, rootComment);
            // depth == 1

            given(mockRequest.getParentCommentSeq()).willReturn(20L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
            given(postRepository.findById(1L)).willReturn(Optional.of(mockPost));
            given(commentRepository.findByIdWithParent(20L)).willReturn(Optional.of(childComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            commentTransactionalService.createComment(1L, 1L, mockRequest);

            // then
            // 실제 부모가 rootComment(depth=0)여야 하므로 저장된 댓글의 depth는 1
            verify(commentRepository).save(argThat(c -> c.getDepth() == 1));
        }

        @Test
        @DisplayName("존재하지 않는 memberSeq이면 예외가 발생한다")
        void throwsExceptionWhenMemberNotFound() {
            // given
            given(memberRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    commentTransactionalService.createComment(1L, 99L, mockRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 postSeq이면 예외가 발생한다")
        void throwsExceptionWhenPostNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
            given(postRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    commentTransactionalService.createComment(99L, 1L, mockRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 parentCommentSeq이면 예외가 발생한다")
        void throwsExceptionWhenParentCommentNotFound() {
            // given
            given(mockRequest.getParentCommentSeq()).willReturn(999L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
            given(postRepository.findById(1L)).willReturn(Optional.of(mockPost));
            given(commentRepository.findByIdWithParent(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    commentTransactionalService.createComment(1L, 1L, mockRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("부모 댓글을 찾을 수 없습니다.");
        }
    }

    // -------------------------------------------------------------------------
    // updateComment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("댓글 수정 - updateComment")
    class UpdateComment {

        @Test
        @DisplayName("정상적인 요청이면 content가 성공적으로 수정된다")
        void updatesCommentContentSuccessfully() {
            // given
            Comment comment = Comment.createComment("old content", mockPost, mockMember, null);
            CommentUpdateDto request = mock(CommentUpdateDto.class);
            given(request.getContent()).willReturn("new content");
            given(commentRepository.findActiveCommentBySeqAndMemberSeq(1L, 1L))
                    .willReturn(Optional.of(comment));

            // when
            commentTransactionalService.updateComment(1L, 1L, request);

            // then
            assertThat(comment.getContent()).isEqualTo("new content");
        }

        @Test
        @DisplayName("댓글이 없거나 수정 권한이 없으면 예외가 발생한다")
        void throwsExceptionWhenCommentNotFoundOrNoPermission() {
            // given
            CommentUpdateDto request = mock(CommentUpdateDto.class);
            given(commentRepository.findActiveCommentBySeqAndMemberSeq(1L, 99L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    commentTransactionalService.updateComment(1L, 99L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글이 없거나 수정 권한이 없습니다.");
        }
    }

    // -------------------------------------------------------------------------
    // deleteComment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("댓글 삭제 - deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("소프트 삭제 시 status가 DELETED로 변경되고 deletedTime이 기록된다")
        void softDeletesCommentAndSetsStatusToDeleted() {
            // given
            Comment comment = Comment.createComment("content", mockPost, mockMember, null);
            given(commentRepository.findActiveCommentBySeqAndMemberSeq(1L, 1L))
                    .willReturn(Optional.of(comment));

            // when
            commentTransactionalService.deleteComment(1L, 1L);

            // then
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
            assertThat(comment.getDeletedTime()).isNotNull();
        }

        @Test
        @DisplayName("댓글이 없거나 삭제 권한이 없으면 예외가 발생한다")
        void throwsExceptionWhenCommentNotFoundOrNoPermission() {
            // given
            given(commentRepository.findActiveCommentBySeqAndMemberSeq(1L, 99L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    commentTransactionalService.deleteComment(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글이 없거나 삭제 권한이 없습니다.");
        }
    }

    // -------------------------------------------------------------------------
    // purgeOldSoftDeletedComments
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("만료된 소프트 삭제 댓글 영구 삭제 - purgeOldSoftDeletedComments")
    class PurgeOldSoftDeletedComments {

        @Test
        @DisplayName("영구 삭제 대상 댓글들을 batch로 삭제한다")
        void deletesAllPurgeableCommentsInBatch() {
            // given
            Comment comment1 = Comment.createComment("c1", mockPost, mockMember, null);
            Comment comment2 = Comment.createComment("c2", mockPost, mockMember, null);
            LocalDateTime cutoff = LocalDateTime.now().minusDays(1);

            given(commentRepository.findPurgableComments(cutoff))
                    .willReturn(List.of(comment1, comment2));

            // when
            commentTransactionalService.purgeOldSoftDeletedComments(cutoff);

            // then
            verify(commentRepository).deleteAllInBatch(List.of(comment1, comment2));
        }

        @Test
        @DisplayName("영구 삭제 대상이 없으면 batch delete를 호출하지 않는다")
        void skipsBatchDeleteWhenNoPurgeableCommentsExist() {
            // given
            LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
            given(commentRepository.findPurgableComments(cutoff)).willReturn(List.of());

            // when
            commentTransactionalService.purgeOldSoftDeletedComments(cutoff);

            // then
            verify(commentRepository, never()).deleteAllInBatch(any());
        }
    }
}