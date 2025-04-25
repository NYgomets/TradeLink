package site.tradelink.tradelink.oauth2.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

public interface ProviderUser {

    public String getId();
    public String getUsername();
    public String getEmail();
    public String getProvider();
    public Map<String, Object> getAttributes();
}
