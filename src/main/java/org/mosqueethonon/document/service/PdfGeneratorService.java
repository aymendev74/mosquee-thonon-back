package org.mosqueethonon.document.service;

import java.util.Map;

public interface PdfGeneratorService {

    byte[] generatePdf(String templateName, Map<String, Object> variables);

}
