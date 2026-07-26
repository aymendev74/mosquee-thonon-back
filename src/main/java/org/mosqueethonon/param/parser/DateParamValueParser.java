package org.mosqueethonon.param.parser;

import org.mosqueethonon.common.config.APIDateFormats;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DateParamValueParser extends ParamValueParser<LocalDate> {

    @Override
    public LocalDate getValue(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(APIDateFormats.DATE_FORMAT));
    }

}
