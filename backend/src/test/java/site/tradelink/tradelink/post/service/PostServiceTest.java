package site.tradelink.tradelink.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.post.request.PostCreateDto;
import site.tradelink.tradelink.post.request.PostUpdateDto;
import site.tradelink.tradelink.post.response.PostResponseDto;
import site.tradelink.tradelink.post.response.PreSignedUrlDto;
import site.tradelink.tradelink.post.service.file.FileUrlService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostTransactionalService transactionalService;

    @Mock
    private FileUrlService fileUrlService;

    @Nested
    @DisplayName("Pre-signed URL 발급")
    class IssueUploadUrl {

        @Test
        @DisplayName("허용된 MIME 타입이면 Pre-signed URL을 반환한다")
        void returnsPreSignedUrlWhenAllowedMimeType() {
            // given
            String originalFilename = "photo.jpg";
            String contentType = "image/jpeg";
            PreSignedUrlDto expected = PreSignedUrlDto.builder()
                    .preSignedUrl("https://s3.example.com/presigned")
                    .s3Key("postPhoto/uuid.jpg")
                    .build();

            given(fileUrlService.issueUploadUrl(originalFilename, contentType)).willReturn(expected);

            // when
            PreSignedUrlDto result = postService.issueUploadUrl(originalFilename, contentType);

            // then
            assertThat(result.getPreSignedUrl()).isEqualTo(expected.getPreSignedUrl());
            assertThat(result.getS3Key()).isEqualTo(expected.getS3Key());
        }

        @Test
        @DisplayName("허용되지 않은 MIME 타입이면 예외를 던진다")
        void throwsExceptionWhenUnsupportedMimeType() {
            // given
            String originalFilename = "doc.pdf";
            String contentType = "application/pdf";

            // when & then
            assertThatThrownBy(() -> postService.issueUploadUrl(originalFilename, contentType))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("지원하지 않는 이미지 파일 형식입니다.");
        }

        @Test
        @DisplayName("contentType이 null이면 예외를 던진다")
        void throwsExceptionWhenContentTypeIsNull() {
            // given
            String originalFilename = "photo.jpg";
            String contentType = null;

            // when & then
            assertThatThrownBy(() -> postService.issueUploadUrl(originalFilename, contentType))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreatePost {

        @Test
        @DisplayName("정상 요청이면 게시글 seq를 반환한다")
        void returnsPostSeqWhenValidRequest() {
            // given
            PostCreateDto request = PostCreateDto.builder()
                    .title("제목")
                    .content("내용")
                    .s3Keys(List.of("postPhoto/uuid.jpg"))
                    .build();
            Long memberSeq = 1L;
            Long expectedPostSeq = 10L;

            given(transactionalService.createPostAndMetadata(request, memberSeq)).willReturn(expectedPostSeq);

            // when
            Long result = postService.createPost(request, memberSeq);

            // then
            assertThat(result).isEqualTo(expectedPostSeq);
            then(transactionalService).should(times(1)).createPostAndMetadata(request, memberSeq);
        }
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class GetPost {

        @Test
        @DisplayName("존재하는 게시글 seq로 조회하면 PostResponseDto를 반환한다")
        void returnsPostResponseDtoWhenPostExists() {
            // given
            Long postSeq = 1L;
            PostResponseDto expected = PostResponseDto.builder()
                    .postSeq(postSeq)
                    .title("제목")
                    .content("내용")
                    .authorName("작성자")
                    .imageUrls(List.of("https://s3.example.com/image.jpg"))
                    .createdAt(LocalDateTime.now())
                    .build();

            given(transactionalService.getPostDetails(postSeq)).willReturn(expected);

            // when
            PostResponseDto result = postService.getPost(postSeq);

            // then
            assertThat(result.getPostSeq()).isEqualTo(postSeq);
            assertThat(result.getTitle()).isEqualTo("제목");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("정상 요청이면 transactionalService의 updatePost를 호출한다")
        void callsUpdatePostWhenValidRequest() {
            // given
            Long postSeq = 1L;
            Long memberSeq = 1L;
            PostUpdateDto updateDto = PostUpdateDto.builder()
                    .title("수정 제목")
                    .content("수정 내용")
                    .s3Keys(List.of("postPhoto/new-uuid.jpg"))
                    .build();

            // when
            postService.updatePost(postSeq, memberSeq, updateDto);

            // then
            then(transactionalService).should(times(1)).updatePost(postSeq, memberSeq, updateDto);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePost {

        @Test
        @DisplayName("정상 요청이면 transactionalService의 softDeletePost를 호출한다")
        void callsSoftDeletePostWhenValidRequest() {
            // given
            Long postSeq = 1L;
            Long memberSeq = 1L;

            // when
            postService.deletePost(postSeq, memberSeq);

            // then
            then(transactionalService).should(times(1)).softDeletePost(postSeq, memberSeq);
        }
    }
}