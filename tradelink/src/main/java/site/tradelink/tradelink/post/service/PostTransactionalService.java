package site.tradelink.tradelink.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.entity.UploadFile;
import site.tradelink.tradelink.post.repository.PostRepository;
import site.tradelink.tradelink.post.request.PostCreateDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTransactionalService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public void createPostAndMetadata(PostCreateDto request, Long memberSeq) {
        // 추후 Error 작업 추가
        Member member = memberRepository.findBySeq(memberSeq)
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
                            .post(post)
                            .build())
                    .toList();
            post.attachFiles(uploadFiles);
        }

        postRepository.save(post);
    }

    public List<String> deletePostAndGetS3Keys(Long postSeq, Long memberSeq) {
        // 추후 Error 작업 추가
        Post post = postRepository.findBySeqAndMemberSeq(postSeq, memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없거나 삭제할 권한이 없습니다."));

        List<String> s3Keys = post.getUploadFiles().stream()
                .map(UploadFile::getS3Key)
                .toList();

        postRepository.delete(post);

        return s3Keys;
    }
}
