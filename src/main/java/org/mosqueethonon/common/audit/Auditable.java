package org.mosqueethonon.common.audit;

public interface Auditable {

    public Signature getSignature();

    public void setSignature(Signature signature);

}
