package org.mosqueethonon.enums;

public enum JourActiviteEnum {

    SAMEDI_MATIN("SAM_MAT"),
    DIMANCHE_MATIN("DIM_MAT"),
    DIMANCHE_APRES_MIDI("DIM_APM"),
    MERCREDI_APRES_MIDI("MER_APM");

    private String value;

    private JourActiviteEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
