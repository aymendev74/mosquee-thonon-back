package org.mosqueethonon.service.impl.document;

import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;

import java.io.InputStream;
import java.io.Reader;

public class ClasspathStreamFactory implements FSStreamFactory {

    @Override
    public FSStream getUrl(String url) {
        if (!url.startsWith("classpath:")) {
            throw new IllegalArgumentException("ClasspathStreamFactory ne gère que le protocole classpath:, reçu : " + url);
        }
        String path = url.substring("classpath:".length());
        InputStream is = ClasspathStreamFactory.class.getResourceAsStream(path);
        if (is == null) {
            throw new RuntimeException("Ressource classpath introuvable : " + path);
        }

        return new FSStream() {
            @Override
            public InputStream getStream() {
                return is;
            }

            @Override
            public Reader getReader() {
                return null;
            }
        };
    }

}
