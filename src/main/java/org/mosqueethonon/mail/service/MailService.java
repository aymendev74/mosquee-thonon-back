package org.mosqueethonon.mail.service;

import org.mosqueethonon.mail.dto.MailDto;

public interface MailService {

    MailDto createMail(Long businessId);

}
