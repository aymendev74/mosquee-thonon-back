package org.mosqueethonon.service.mail;

import org.mosqueethonon.dto.mail.MailDto;

public interface MailService {

    MailDto createMail(Long businessId);

}
