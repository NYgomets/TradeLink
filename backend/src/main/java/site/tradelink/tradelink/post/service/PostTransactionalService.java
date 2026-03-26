package site.tradelink.tradelink.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.comment.repository.CommentRepository;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;
import site.tradelink.tradelink.post.common.enums.FileStatus;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.entity.UploadFile;
import site.tradelink.tradelink.post.repository.PostRepository;
import site.tradelink.tradelink.post.repository.file.UploadFileRepository;
import site.tradelink.tradelink.post.request.PostCreateDto;
import site.tradelink.tradelink.post.request.PostUpdateDto;
import site.tradelink.tradelink.post.response.PostResponseDto;
import site.tradelink.tradelink.post.response.PostSummaryDto;
import site.tradelink.tradelink.post.service.file.FileUrlService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTransactionalService {

    private final FileUrlService fileUrlService;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final UploadFileRepository uploadFileRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Long createPostAndMetadata(PostCreateDto request, Long memberSeq) {
        // 추후 Error 작업 추가
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
                .build();

        List<String> s3Keys = request.getS3Keys();
        if (s3Keys != null && !s3Keys.isEmpty()) {
            List<UploadFile> uploadFiles = s3Keys.stream()
                    .map(s3Key -> UploadFile.builder()
                            .s3Key(s3Key)
                            .build())
                    .toList();
            post.attachFiles(uploadFiles);
        }

        return postRepository.save(post).getSeq();
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPostDetails(Long postSeq) {
        // 추후 Error 작업 추가
        Post post = postRepository.findActivePostWithDetailsBySeq(postSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<String> imageUrls = uploadFileRepository.findByPostSeq(postSeq).stream()
                .map(file -> fileUrlService.issueDownloadUrl(file.getS3Key()))
                .toList();

        return PostResponseDto.builder()
                .postSeq(post.getSeq())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getMember().getMemberName())
                .imageUrls(imageUrls)
                .createdAt(post.getCreateTime())
                .build();
    }


    @Transactional
    public void updatePost(Long postSeq, Long memberSeq, PostUpdateDto updateDto) {
        // 추후 Error 작업 추가
        Post post = postRepository.findActivePostBySeqAndMemberSeq(postSeq, memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없거나 수정 권한이 없습니다."));

        post.update(updateDto.getTitle(), updateDto.getContent());

        if (updateDto.getS3Keys() != null) {
            List<UploadFile> currentFiles = uploadFileRepository.findByPostSeq(postSeq);
            updatePostFiles(post, currentFiles, updateDto.getS3Keys());
        }
    }

    private void updatePostFiles(Post post, List<UploadFile> currentFiles, List<String> newS3Keys) {
        Map<String, UploadFile> existingFilesMap = currentFiles.stream()
                .collect(Collectors.toMap(UploadFile::getS3Key, file -> file));

        Set<String> newKeysSet = new HashSet<>(newS3Keys);

        // 삭제할 파일은 DB에서 softDelete 처리
        existingFilesMap.forEach((existingKey, existingFile) -> {
            if (!newKeysSet.contains(existingKey)) {
                existingFile.softDelete();
            }
        });

        // 새로 추가된 파일은 Post에 추가
        newKeysSet.forEach(newKey -> {
            if (!existingFilesMap.containsKey(newKey)) {
                UploadFile newFile = UploadFile.builder()
                        .s3Key(newKey)
                        .build();

                post.addUploadFile(newFile);
            }
        });
    }

    @Transactional
    public void softDeletePost(Long postSeq, Long memberSeq) {
        // 추후 Error 작업 추가
        Post post = postRepository.findActivePostBySeqAndMemberSeq(postSeq, memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없거나 삭제 권한이 없습니다."));

        post.softDelete();
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findActivePostsWithMember(pageable);

        List<Long> postSeqs = posts.stream().map(Post::getSeq).toList();

        if (postSeqs.isEmpty()) return posts.map(p -> new PostSummaryDto(p, 0, false));

        // 댓글 수: IN 쿼리 1번
        Map<Long, Integer> commentCounts = postRepository.countCommentsByPostSeqs(postSeqs)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // 파일 존재 여부: DISTINCT postSeq만 가져옴 (파일 수와 무관하게 경량)
        Set<Long> postSeqsWithFiles = uploadFileRepository
                .findPostSeqsHavingActiveFiles(postSeqs);

        return posts.map(post -> new PostSummaryDto(
                post,
                commentCounts.getOrDefault(post.getSeq(), 0),
                postSeqsWithFiles.contains(post.getSeq())
        ));
    }
}
