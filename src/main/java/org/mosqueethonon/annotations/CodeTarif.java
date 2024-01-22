package org.mosqueethonon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.FIELD )
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeTarif {

    public String codeTarif();
    public int nbEnfant();
    public String type();

    public boolean adherent();

}
