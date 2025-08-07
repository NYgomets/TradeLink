package site.tradelink.tradelink.oauth2.common.converters;

public interface ProviderUserConverter<T1, T2, R> {
    R convert(T1 t1, T2 t2);
}
