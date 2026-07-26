package org.mosqueethonon.mail.enums;

public enum MailRequestType {
    INSCRIPTION,
    ADHESION;

    public String getValue() {
        return this.name();
    }
}
