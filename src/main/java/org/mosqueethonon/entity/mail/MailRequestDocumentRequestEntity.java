package org.mosqueethonon.entity.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "mail_request_document_request", schema = "moth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailRequestDocumentRequestEntity {

    @Id
    @Column(name = "idmrdr", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idmare", nullable = false, insertable = false, updatable = false)
    private Long mailRequestId;

    @Column(name = "iddore", nullable = false)
    private Long documentRequestId;

}
