package org.mosqueethonon.enums;

public enum MailRequestType {
    INSCRIPTION,
    ADHESION;

    public String getValue() {
        return this.name();
    }
}
