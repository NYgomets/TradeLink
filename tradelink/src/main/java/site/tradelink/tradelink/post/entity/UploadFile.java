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
@Table(indexes = {
        @Index(name = "idx_upload_file_post_seq", columnList = "post_seq"),
        @Index(name = "idx_upload_file_status_deleted_time", columnList = "status, deleted_time"),
        @Index(name = "idx_upload_file_s3_key", columnList = "s3Key")
})
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
