package org.mosqueethonon.param.parser;

public abstract class ParamValueParser<T> {

    public abstract T getValue(String value);

}
