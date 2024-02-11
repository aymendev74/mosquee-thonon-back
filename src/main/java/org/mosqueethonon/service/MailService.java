package org.mosqueethonon.service;

import org.mosqueethonon.enums.TypeMailEnum;
import org.mosqueethonon.v1.dto.MailObjectDto;

public interface MailService {

    public void sendEmailConfirmation(MailObjectDto mailObject, TypeMailEnum typeMail);

}
