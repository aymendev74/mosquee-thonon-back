package org.mosqueethonon.params;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class ParamParser {

    private BooleanParamValueParser booleanParamValueParser;

    private DateParamValueParser dateParamValueParser;

    public Object parseValue(String paramValue, Class<?> classe) {
        if(paramValue == null || StringUtils.EMPTY.equals(paramValue)) {
            return null;
        } else if(classe == Boolean.class || classe == boolean.class) {
            return this.booleanParamValueParser.getValue(paramValue);
        } else if(classe == LocalDate.class) {
            return this.dateParamValueParser.getValue(paramValue);
        }
        throw new IllegalArgumentException("type de paramètre non géré : " + classe.toString());
    }
}
