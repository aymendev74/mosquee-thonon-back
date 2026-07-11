package org.mosqueethonon.entity.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mosqueethonon.enums.DocumentMetadataKey;

@Entity
@Table(name = "document_metadata", schema = "moth")
@Getter
@Setter
@NoArgsConstructor
public class DocumentMetadataEntity {

    @Id
    @Column(name = "iddome")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iddocu", insertable = false, updatable = false)
    private Long idDocument;

    @Column(name = "cddomecle")
    @Enumerated(EnumType.STRING)
    private DocumentMetadataKey cle;

    @Column(name = "txdomevaleur")
    private String valeur;

    public DocumentMetadataEntity(DocumentMetadataKey cle, String valeur) {
        this.cle = cle;
        this.valeur = valeur;
    }

}
