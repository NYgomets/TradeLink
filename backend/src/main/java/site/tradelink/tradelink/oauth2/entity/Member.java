package site.tradelink.tradelink.oauth2.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Builder
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@AllArgsConstructor (access = AccessLevel.PRIVATE)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "member_seq")
    private Long seq;

    @Column(unique = true, nullable = false)
    private String memberId;

    private String memberName;

    private String provider;

    @Column(nullable = false)
    private String email;
}
