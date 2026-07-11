package org.mosqueethonon.dto.mail;

import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class MailDto {

    private String recipientEmail;
    private String subject;
    private String body;
    @Singular
    private List<MailAttachmentDto> attachments;

    public void addAttachments(List<MailAttachmentDto> attachments) {
        if(this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.addAll(attachments);
    }
}
