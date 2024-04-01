package org.mosqueethonon.params;

public abstract class ParamValueParser<T> {

    public abstract T getValue(String value);

}
