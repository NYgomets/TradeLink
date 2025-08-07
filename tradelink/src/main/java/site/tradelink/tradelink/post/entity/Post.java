package site.tradelink.tradelink.post.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.common.entity.BaseEntity;
import site.tradelink.tradelink.oauth2.entity.Member;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "post_seq")
    private Long seq;

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
