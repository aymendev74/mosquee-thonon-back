package org.mosqueethonon.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.mosqueethonon.enums.JourActiviteEnum;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class JourActiviteConverter implements AttributeConverter<JourActiviteEnum, String> {

    @Override
    public String convertToDatabaseColumn(JourActiviteEnum jourActiviteEnum) {
        return jourActiviteEnum == null ? null : jourActiviteEnum.getValue();
    }

    @Override
    public JourActiviteEnum convertToEntityAttribute(String s) {
        return Stream.of(JourActiviteEnum.values()).filter(enumVal -> enumVal.getValue().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
