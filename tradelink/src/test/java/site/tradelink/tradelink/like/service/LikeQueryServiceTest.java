package site.tradelink.tradelink.like.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import site.tradelink.tradelink.like.entity.LikeStatus;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.response.LikeStatusResponseDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LikeQueryServiceTest {

    @InjectMocks
    private LikeQueryService likeQueryService;

    @Mock
    private LikeStatusRepository likeStatusRepository;

    @Mock
    private PostStatsRepository postStatsRepository;

    @Nested
    @DisplayName("getLikeStatus()")
    class GetLikeStatus {

        @Test
        @DisplayName("좋아요를 누른 회원 → isLiked=true, likeCount 정상 반환")
        void getLikeStatus_whenLiked_returnTrueAndCount() {
            // given
            Long memberSeq = 1L;
            Long postSeq = 100L;

            LikeStatus likeStatus = LikeStatus.builder()
                    .memberSeq(memberSeq)
                    .postSeq(postSeq)
                    .isLiked(true)
                    .build();

            PostStats postStats = PostStats.builder()
                    .postSeq(postSeq)
                    .likeCount(42L)
                    .build();

            given(likeStatusRepository.findByMemberSeqAndPostSeq(memberSeq, postSeq))
                    .willReturn(Optional.of(likeStatus));
            given(postStatsRepository.findByPostSeq(postSeq))
                    .willReturn(Optional.of(postStats));

            // when
            LikeStatusResponseDto result = likeQueryService.getLikeStatus(memberSeq, postSeq);

            // then
            assertThat(result.getPostSeq()).isEqualTo(postSeq);
            assertThat(result.getIsLiked()).isTrue();
            assertThat(result.getLikeCount()).isEqualTo(42L);
        }

        @Test
        @DisplayName("좋아요를 누르지 않은 회원 → isLiked=false 반환")
        void getLikeStatus_whenNotLiked_returnFalse() {
            // given
            Long memberSeq = 1L;
            Long postSeq = 100L;

            LikeStatus likeStatus = LikeStatus.builder()
                    .memberSeq(memberSeq)
                    .postSeq(postSeq)
                    .isLiked(false)
                    .build();

            PostStats postStats = PostStats.builder()
                    .postSeq(postSeq)
                    .likeCount(10L)
                    .build();

            given(likeStatusRepository.findByMemberSeqAndPostSeq(memberSeq, postSeq))
                    .willReturn(Optional.of(likeStatus));
            given(postStatsRepository.findByPostSeq(postSeq))
                    .willReturn(Optional.of(postStats));

            // when
            LikeStatusResponseDto result = likeQueryService.getLikeStatus(memberSeq, postSeq);

            // then
            assertThat(result.getIsLiked()).isFalse();
        }

        @Test
        @DisplayName("비로그인 유저 (memberSeq=null) → isLiked=false, likeCount 정상 반환")
        void getLikeStatus_whenGuestUser_returnFalseAndCount() {
            // given
            Long memberSeq = null;
            Long postSeq = 100L;

            PostStats postStats = PostStats.builder()
                    .postSeq(postSeq)
                    .likeCount(7L)
                    .build();

            given(postStatsRepository.findByPostSeq(postSeq))
                    .willReturn(Optional.of(postStats));

            // when
            LikeStatusResponseDto result = likeQueryService.getLikeStatus(memberSeq, postSeq);

            // then
            assertThat(result.getIsLiked()).isFalse();
            assertThat(result.getLikeCount()).isEqualTo(7L);
        }

        @Test
        @DisplayName("PostStats가 없는 게시글 → likeCount=0 반환")
        void getLikeStatus_whenNoPostStats_returnZeroCount() {
            // given
            Long memberSeq = 1L;
            Long postSeq = 999L;

            given(likeStatusRepository.findByMemberSeqAndPostSeq(memberSeq, postSeq))
                    .willReturn(Optional.empty());
            given(postStatsRepository.findByPostSeq(postSeq))
                    .willReturn(Optional.empty());

            // when
            LikeStatusResponseDto result = likeQueryService.getLikeStatus(memberSeq, postSeq);

            // then
            assertThat(result.getIsLiked()).isFalse();
            assertThat(result.getLikeCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getMyLikedPostSeqs()")
    class GetMyLikedPostSeqs {

        @Test
        @DisplayName("좋아요한 게시글 목록 페이징 조회 → postSeq 목록 반환")
        void getMyLikedPostSeqs_returnPagedPostSeqs() {
            // given
            Long memberSeq = 1L;
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Long> expected = new PageImpl<>(List.of(100L, 200L, 300L));

            given(likeStatusRepository.findLikedPostSeqs(memberSeq, pageable))
                    .willReturn(expected);

            // when
            Page<Long> result = likeQueryService.getMyLikedPostSeqs(memberSeq, pageable);

            // then
            assertThat(result.getContent()).containsExactly(100L, 200L, 300L);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("좋아요한 게시글이 없는 경우 → 빈 페이지 반환")
        void getMyLikedPostSeqs_whenEmpty_returnEmptyPage() {
            // given
            Long memberSeq = 1L;
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Long> emptyPage = Page.empty();

            given(likeStatusRepository.findLikedPostSeqs(memberSeq, pageable))
                    .willReturn(emptyPage);

            // when
            Page<Long> result = likeQueryService.getMyLikedPostSeqs(memberSeq, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }
}