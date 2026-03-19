package site.tradelink.tradelink.cryptocurrency.handler;

import java.util.Map;

public final class CryptoName {

    private CryptoName() {}

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("BTC",   "비트코인"),
            Map.entry("ETH",   "이더리움"),
            Map.entry("XRP",   "리플"),
            Map.entry("ADA",   "에이다"),
            Map.entry("DOGE",  "도지코인"),
            Map.entry("LINK",  "체인링크"),
            Map.entry("TRX",   "트론"),
            Map.entry("LTC",   "라이트코인"),
            Map.entry("BCH",   "비트코인캐시"),
            Map.entry("ETC",   "이더리움클래식")
    );

    public static String of(String ticker) {
        return NAMES.getOrDefault(ticker, ticker);
    }
}
