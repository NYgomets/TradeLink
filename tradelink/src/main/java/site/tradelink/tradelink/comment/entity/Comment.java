package site.tradelink.tradelink.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.common.entity.BaseEntity;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.post.entity.Post;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "comment_seq")
    private Long seq;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_seq")
    private Post post;
}
