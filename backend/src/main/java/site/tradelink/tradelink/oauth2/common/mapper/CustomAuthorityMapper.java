package site.tradelink.tradelink.oauth2.common.mapper;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomAuthorityMapper implements GrantedAuthoritiesMapper {

    private String prefix = "ROLE_";

    @Override
    public Set<GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        HashSet<GrantedAuthority> mapped = new HashSet(authorities.size());

        for(GrantedAuthority authority : authorities) {
            mapped.add(this.mapAuthority(authority.getAuthority()));
        }
        return mapped;
    }

    private GrantedAuthority mapAuthority(String name) {
        if (name.startsWith("OAUTH") || name.startsWith("OIDC")) {
            if (name.lastIndexOf("_") > 0) {
                int index = name.lastIndexOf("_");
                name = name.substring(index+1);
            }

            if (!name.startsWith(this.prefix)) {
                name = this.prefix + name;
            }

            return new SimpleGrantedAuthority(name);
        }
        return new SimpleGrantedAuthority("anonymousUser");
    }
}
