package org.mosqueethonon.mail.enums;

public enum MailRequestTypeEnum {
    INSCRIPTION,
    ADHESION;

    public String getValue() {
        return this.name();
    }
}
