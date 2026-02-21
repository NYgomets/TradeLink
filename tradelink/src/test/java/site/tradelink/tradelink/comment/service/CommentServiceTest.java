package site.tradelink.tradelink.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.comment.entity.Comment;
import site.tradelink.tradelink.comment.response.CommentResponseDto;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.post.entity.Post;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentTransactionalService transactionalService;

    private Member mockMember;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        mockMember = mock(Member.class);
        mockPost = mock(Post.class);

        given(mockMember.getMemberName()).willReturn("testUser");
        given(mockPost.getSeq()).willReturn(1L);
    }

    @Nested
    @DisplayName("게시글 댓글 목록 조회 - getCommentsForPost")
    class GetCommentsForPost {

        @Test
        @DisplayName("루트 댓글과 대댓글이 트리 구조로 올바르게 조립된다")
        void returnsRootCommentsWithRepliesNestedCorrectly() {
            // given
            Comment rootComment = Comment.createComment("root content", mockPost, mockMember, null);
            Comment childComment = Comment.createComment("child content", mockPost, mockMember, rootComment);

            given(transactionalService.getCommentsByPostSeq(1L))
                    .willReturn(List.of(rootComment, childComment));

            // when
            List<CommentResponseDto> result = commentService.getCommentsForPost(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).content()).isEqualTo("root content");
            assertThat(result.get(0).replies()).hasSize(1);
            assertThat(result.get(0).replies().get(0).content()).isEqualTo("child content");
        }

        @Test
        @DisplayName("댓글이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoCommentsExist() {
            // given
            given(transactionalService.getCommentsByPostSeq(1L)).willReturn(List.of());

            // when
            List<CommentResponseDto> result = commentService.getCommentsForPost(1L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("삭제된 댓글은 content 대신 안내 문구를 반환한다")
        void returnsDeletedCommentContentAsPlaceholderMessage() {
            // given
            Comment deletedComment = Comment.createComment("original content", mockPost, mockMember, null);
            deletedComment.softDelete();

            given(transactionalService.getCommentsByPostSeq(1L))
                    .willReturn(List.of(deletedComment));

            // when
            List<CommentResponseDto> result = commentService.getCommentsForPost(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).content()).isEqualTo("삭제된 댓글입니다.");
        }

        @Test
        @DisplayName("반환된 리스트는 수정 불가능한 리스트이다")
        void returnsResultAsUnmodifiableList() {
            // given
            given(transactionalService.getCommentsByPostSeq(1L)).willReturn(List.of());

            // when
            List<CommentResponseDto> result = commentService.getCommentsForPost(1L);

            // then
            assertThat(result).isUnmodifiable();
        }

        @Test
        @DisplayName("루트 댓글의 depth는 0, 대댓글의 depth는 1이다")
        void keepsDepthInfoCorrectlyForRootAndChildComments() {
            // given
            Comment rootComment = Comment.createComment("root", mockPost, mockMember, null);
            Comment childComment = Comment.createComment("child", mockPost, mockMember, rootComment);

            given(transactionalService.getCommentsByPostSeq(1L))
                    .willReturn(List.of(rootComment, childComment));

            // when
            List<CommentResponseDto> result = commentService.getCommentsForPost(1L);

            // then
            assertThat(result.get(0).depth()).isEqualTo(0);
            assertThat(result.get(0).replies().get(0).depth()).isEqualTo(1);
        }
    }
}
