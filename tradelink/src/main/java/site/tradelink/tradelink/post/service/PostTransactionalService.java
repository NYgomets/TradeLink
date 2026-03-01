package site.tradelink.tradelink.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;
import site.tradelink.tradelink.post.common.enums.PostStatus;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.entity.UploadFile;
import site.tradelink.tradelink.post.repository.PostRepository;
import site.tradelink.tradelink.post.request.PostCreateDto;
import site.tradelink.tradelink.post.request.PostUpdateDto;
import site.tradelink.tradelink.post.response.PostResponseDto;
import site.tradelink.tradelink.post.service.file.FileUrlService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTransactionalService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final FileUrlService fileUrlService;

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

        List<String> imageUrls = post.getUploadFiles().stream()
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
        Post post = postRepository.findActivePostWithFilesBySeqAndMemberSeq(postSeq, memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없거나 수정 권한이 없습니다."));

        post.update(updateDto.getTitle(), updateDto.getContent());

        if (updateDto.getS3Keys() != null) {
            updatePostFiles(post, updateDto.getS3Keys());
        }
    }

    private void updatePostFiles(Post post, List<String> newS3Keys) {
        Map<String, UploadFile> existingFilesMap = post.getUploadFiles().stream()
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
}
