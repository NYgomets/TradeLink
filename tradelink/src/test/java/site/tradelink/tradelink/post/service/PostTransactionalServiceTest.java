package site.tradelink.tradelink.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;
import site.tradelink.tradelink.post.common.enums.PostStatus;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.repository.PostRepository;
import site.tradelink.tradelink.post.request.PostCreateDto;
import site.tradelink.tradelink.post.request.PostUpdateDto;
import site.tradelink.tradelink.post.response.PostResponseDto;
import site.tradelink.tradelink.post.service.file.FileUrlService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PostTransactionalServiceTest {

    @InjectMocks
    private PostTransactionalService postTransactionalService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileUrlService fileUrlService;

    @Nested
    @DisplayName("게시글 생성")
    class CreatePostAndMetadata {

        @Test
        @DisplayName("존재하는 회원이면 게시글을 저장하고 seq를 반환한다")
        void savesPostAndReturnsSeqWhenMemberExists() {
            // given
            Long memberSeq = 1L;
            Member member = Member.builder().memberName("테스터").build();

            PostCreateDto request = PostCreateDto.builder()
                    .title("제목")
                    .content("내용")
                    .s3Keys(List.of("postPhoto/uuid.jpg"))
                    .build();

            Post savedPost = Post.builder()
                    .title("제목")
                    .content("내용")
                    .member(member)
                    .build();

            given(memberRepository.findById(memberSeq)).willReturn(Optional.of(member));
            given(postRepository.save(any(Post.class))).willReturn(savedPost);

            // when
            postTransactionalService.createPostAndMetadata(request, memberSeq);

            // then
            then(postRepository).should(times(1)).save(any(Post.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 예외를 던진다")
        void throwsExceptionWhenMemberNotFound() {
            // given
            Long memberSeq = 999L;
            PostCreateDto request = PostCreateDto.builder()
                    .title("제목")
                    .content("내용")
                    .s3Keys(List.of())
                    .build();

            given(memberRepository.findById(memberSeq)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postTransactionalService.createPostAndMetadata(request, memberSeq))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("s3Keys가 null이면 파일 없이 게시글을 저장한다")
        void savesPostWithoutFilesWhenS3KeysIsNull() {
            // given
            Long memberSeq = 1L;
            Member member = Member.builder().memberName("테스터").build();

            PostCreateDto request = PostCreateDto.builder()
                    .title("제목")
                    .content("내용")
                    .s3Keys(null)
                    .build();

            Post savedPost = Post.builder()
                    .title("제목")
                    .content("내용")
                    .member(member)
                    .build();

            given(memberRepository.findById(memberSeq)).willReturn(Optional.of(member));
            given(postRepository.save(any(Post.class))).willReturn(savedPost);

            // when
            postTransactionalService.createPostAndMetadata(request, memberSeq);

            // then
            then(postRepository).should(times(1)).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class GetPostDetails {

        @Test
        @DisplayName("존재하는 게시글을 조회하면 PostResponseDto를 반환한다")
        void returnsPostResponseDtoWhenPostExists() {
            // given
            Long postSeq = 1L;
            Member member = Member.builder().memberName("작성자").build();

            Post post = Post.builder()
                    .title("제목")
                    .content("내용")
                    .member(member)
                    .build();

            given(postRepository.findActivePostWithDetailsBySeq(postSeq)).willReturn(Optional.of(post));
            given(fileUrlService.issueDownloadUrl("postPhoto/uuid.jpg"))
                    .willReturn("https://s3.example.com/presigned-url");

            // when
            PostResponseDto result = postTransactionalService.getPostDetails(postSeq);

            // then
            assertThat(result.getTitle()).isEqualTo("제목");
            assertThat(result.getContent()).isEqualTo("내용");
            assertThat(result.getAuthorName()).isEqualTo("작성자");
        }

        @Test
        @DisplayName("존재하지 않는 게시글을 조회하면 예외를 던진다")
        void throwsExceptionWhenPostNotFound() {
            // given
            Long postSeq = 999L;
            given(postRepository.findActivePostWithDetailsBySeq(postSeq)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postTransactionalService.getPostDetails(postSeq))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("권한이 있는 회원이 수정 요청하면 제목과 내용이 변경된다")
        void updatesTitleAndContentWhenAuthorizedMember() {
            // given
            Long postSeq = 1L;
            Long memberSeq = 1L;
            Member member = Member.builder().memberName("작성자").build();

            Post post = Post.builder()
                    .title("기존 제목")
                    .content("기존 내용")
                    .member(member)
                    .build();

            PostUpdateDto updateDto = PostUpdateDto.builder()
                    .title("수정 제목")
                    .content("수정 내용")
                    .s3Keys(List.of())
                    .build();

            given(postRepository.findActivePostWithFilesBySeqAndMemberSeq(postSeq, memberSeq))
                    .willReturn(Optional.of(post));

            // when
            postTransactionalService.updatePost(postSeq, memberSeq, updateDto);

            // then
            assertThat(post.getTitle()).isEqualTo("수정 제목");
            assertThat(post.getContent()).isEqualTo("수정 내용");
        }

        @Test
        @DisplayName("권한이 없는 회원이 수정 요청하면 예외를 던진다")
        void throwsExceptionWhenUnauthorizedMemberUpdates() {
            // given
            Long postSeq = 1L;
            Long memberSeq = 999L;

            PostUpdateDto updateDto = PostUpdateDto.builder()
                    .title("수정 제목")
                    .content("수정 내용")
                    .s3Keys(List.of())
                    .build();

            given(postRepository.findActivePostWithFilesBySeqAndMemberSeq(postSeq, memberSeq))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postTransactionalService.updatePost(postSeq, memberSeq, updateDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글이 없거나 수정 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 소프트 삭제")
    class SoftDeletePost {

        @Test
        @DisplayName("권한이 있는 회원이 삭제 요청하면 게시글 상태가 DELETED로 변경된다")
        void changesStatusToDeletedWhenAuthorizedMember() {
            // given
            Long postSeq = 1L;
            Long memberSeq = 1L;
            Member member = Member.builder().memberName("작성자").build();

            Post post = Post.builder()
                    .title("제목")
                    .content("내용")
                    .member(member)
                    .build();

            given(postRepository.findActivePostBySeqAndMemberSeq(postSeq, memberSeq))
                    .willReturn(Optional.of(post));

            // when
            postTransactionalService.softDeletePost(postSeq, memberSeq);

            // then
            assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
            assertThat(post.getDeletedTime()).isNotNull();
        }

        @Test
        @DisplayName("권한이 없는 회원이 삭제 요청하면 예외를 던진다")
        void throwsExceptionWhenUnauthorizedMemberDeletes() {
            // given
            Long postSeq = 1L;
            Long memberSeq = 999L;

            given(postRepository.findActivePostBySeqAndMemberSeq(postSeq, memberSeq))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postTransactionalService.softDeletePost(postSeq, memberSeq))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글이 없거나 삭제 권한이 없습니다.");
        }
    }
}
