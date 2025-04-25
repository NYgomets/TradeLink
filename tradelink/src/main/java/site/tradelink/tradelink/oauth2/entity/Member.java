package site.tradelink.tradelink.oauth2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(unique = true, nullable = false)
    private String memberId;

    private String memberName;

    private String provider;

    @Column(unique = true, nullable = false)
    private String email;
}
