package site.tradelink.tradelink.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.comment.common.enums.CommentStatus;
import site.tradelink.tradelink.supports.entity.BaseEntity;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.post.entity.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE) // Builder는 static factory method를 통해서만 사용하도록 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "comment_seq")
    private Long seq;

    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private CommentStatus status = CommentStatus.ACTIVE;

    private int depth;

    private LocalDateTime deletedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_seq")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_seq")
    private Post post;

    /**
     * 댓글 생성을 위한 정적 팩토리 메서드
     * 깊이 계산을 캡슐화하여 객체의 일관성을 보장
     */
    public static Comment createComment(String content, Post post, Member member, Comment parent) {
        return Comment.builder()
                .content(content)
                .post(post)
                .member(member)
                .parent(parent)
                .depth((parent != null) ? parent.getDepth() + 1 : 0)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.status = CommentStatus.DELETED;
        this.deletedTime = LocalDateTime.now();
    }
}
