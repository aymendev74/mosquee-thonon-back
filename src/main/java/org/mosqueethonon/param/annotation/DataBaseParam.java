package org.mosqueethonon.param.annotation;

import org.mosqueethonon.param.enums.ParamNameEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.FIELD )
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBaseParam {

    public ParamNameEnum name();

}
