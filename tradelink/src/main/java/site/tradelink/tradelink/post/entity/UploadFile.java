package site.tradelink.tradelink.post.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;
import site.tradelink.tradelink.post.common.enums.FileStatus;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadFile extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uploadFile_seq")
    private Long seq;

    private String s3Key;

    @Enumerated(EnumType.STRING)
    private FileStatus status = FileStatus.ACTIVE;

    private LocalDateTime deletedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_seq")
    private Post post;

    protected void linkToPost(Post post) {
        this.post = post;
    }

    public void softDelete() {
        this.status = FileStatus.DELETED;
        deletedTime = LocalDateTime.now();
    }
}
