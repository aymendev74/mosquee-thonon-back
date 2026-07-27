package org.mosqueethonon.param.parser;

import org.springframework.stereotype.Component;

@Component
public class BooleanParamValueParser extends ParamValueParser<Boolean> {

    @Override
    public Boolean getValue(String paramValue) {
        return Boolean.parseBoolean(paramValue);
    }

}
