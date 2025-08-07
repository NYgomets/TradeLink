package site.tradelink.tradelink.likePost.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.post.entity.Post;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikePost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_post_seq")
    private Long seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_seq")
    private Post post;
}
