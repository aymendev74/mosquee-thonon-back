package org.mosqueethonon.document.service;

import org.mosqueethonon.document.entity.DocumentMetadataEntity;

import java.util.List;
import java.util.Map;

public interface DocumentGenerator<T> {

    String getCode();

    String getPath();

    String getTemplateName();

    String generateFileName(T entity);

    String getAnnee(T entity);

    Long getIdUtilisateur(T entity);

    Map<String, Object> buildTemplateVariables(T entity);

    String computeHash(T entity);

    List<DocumentMetadataEntity> buildMetadata(T entity);

}
