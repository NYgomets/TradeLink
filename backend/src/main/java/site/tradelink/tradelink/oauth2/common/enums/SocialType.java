package site.tradelink.tradelink.oauth2.common.enums;

public enum SocialType {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver");

    private final String socialName;
    SocialType(String socialName) {
        this.socialName = socialName;
    }
    public String getSocialName() {
        return socialName;
    }
}
