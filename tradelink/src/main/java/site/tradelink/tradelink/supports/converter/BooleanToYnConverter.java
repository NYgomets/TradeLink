package site.tradelink.tradelink.supports.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToYnConverter implements AttributeConverter<Boolean, String> {

    /**
     * Boolean 값을 Y 또는 N 으로 컨버트
     *
     * @param attribute  boolean 값
     * @return String // true 인 경우 Y 또는 false 인 경우 N
     */
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return (attribute != null && attribute) ? "Y" : "N";
    }

    /**
     * Y 또는 N 을 Boolean 으로 컨버트
     *
     * @param dbData  String // Y 또는 N
     * @return Boolean // 대, 소문자를 구분짓지 않고 Y 인 경우 true, N 인 경우 false
     */
    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "Y".equalsIgnoreCase(dbData);
    }
}
