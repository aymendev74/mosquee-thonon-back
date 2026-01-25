package org.mosqueethonon.annotations;

import org.mosqueethonon.enums.ResourceTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLock {

    ResourceTypeEnum resourceType();
    String resourceIdParam();

}
