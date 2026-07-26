package org.mosqueethonon.mail.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailAttachmentDto {

    private String name;
    private String location;

}
