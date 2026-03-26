package site.tradelink.tradelink.oauth2.common.principal;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OidcUser {

    private final Long memberSeq;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String name;

    // OidcUser 인터페이스가 요구하는 필드
    private final OidcUserInfo userInfo;
    private final OidcIdToken idToken;

    public CustomOAuth2User(Long seq, Map<String, Object> attributes, Collection<? extends GrantedAuthority> authorities, String name, OidcUserInfo userInfo, OidcIdToken idToken) {
        this.memberSeq = seq;
        this.attributes = attributes;
        this.authorities = authorities;
        this.name = name;
        this.userInfo = userInfo;
        this.idToken = idToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, Object> getClaims() {
        return this.attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return this.userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return this.idToken;
    }
}
