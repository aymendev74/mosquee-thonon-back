package org.mosqueethonon.dto.mail;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MailDto {

    private String recipientEmail;
    private String subject;
    private String body;
    @Singular
    private List<MailAttachmentDto> attachments;

    public void addAttchments(List<MailAttachmentDto> attachments) {
        if(this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.addAll(attachments);
    }
}
