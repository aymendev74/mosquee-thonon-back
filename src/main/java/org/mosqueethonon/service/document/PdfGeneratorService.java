package org.mosqueethonon.service.document;

import java.util.Map;

public interface PdfGeneratorService {

    byte[] generatePdf(String templateName, Map<String, Object> variables);

}
