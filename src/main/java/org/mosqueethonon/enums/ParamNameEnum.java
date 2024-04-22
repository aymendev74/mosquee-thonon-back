package org.mosqueethonon.enums;

public enum ParamNameEnum {

    REINSCRIPTION_ENABLED(ParamTypeEnum.BOOLEAN);

    private ParamTypeEnum type;

    private ParamNameEnum(ParamTypeEnum type) {
        if(type == null) {
            throw new IllegalArgumentException("Un paramètre doit obligatoirement avoir un type !");
        }
        this.type = type;
    }

    public ParamTypeEnum getType() {
        return type;
    }

    public void setType(ParamTypeEnum type) {
        this.type = type;
    }
}
