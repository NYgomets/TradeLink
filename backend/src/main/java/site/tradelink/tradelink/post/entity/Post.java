package site.tradelink.tradelink.post.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.post.common.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(indexes = {
        @Index(name = "idx_post_status_deleted_time", columnList = "status, deleted_time")
})
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "post_seq")
    private Long seq;

    private String title;

    private String content;

    private LocalDateTime deletedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UploadFile> uploadFiles = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.ACTIVE;

    public void attachFiles(List<UploadFile> files) {
        files.forEach(this::addUploadFile);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addUploadFile(UploadFile file) {
        this.uploadFiles.add(file);
        file.linkToPost(this);
    }

    public void softDelete() {
        this.status = PostStatus.DELETED;
        this.deletedTime = LocalDateTime.now();
    }
}
