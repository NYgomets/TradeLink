package site.tradelink.tradelink.oauth2.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Attributes {

    /**
     * ProviderUserConverter 객체에서  OAuth2Utils.getXXXAttributes()가 호출될 때마다
     * 새로운 Attributes 객체를 생성함.
     * 따라서 HashMap을 사용하는 것이 ConcurrentHashMap을 사용하는 것보다 리소스 측면에서 낫아보임.
     */
    private Map<String, Object> mainAttributes;
    private Map<String, Object> subAttributes;
    private Map<String, Object> otherAttributes;
}
