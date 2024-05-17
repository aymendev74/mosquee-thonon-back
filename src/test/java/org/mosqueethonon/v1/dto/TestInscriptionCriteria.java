package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;

import java.time.LocalDate;

public class TestInscriptionCriteria {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    public void testSerializeDeserialize() throws Exception {
        InscriptionCriteria criteria = new InscriptionCriteria();
        criteria.setDateInscription(LocalDate.of(2024, 5, 17));

        // Serialization
        String jsonString = objectMapper.writeValueAsString(criteria);
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("17.05.2024"));

        // Deserialization
        InscriptionCriteria deserializedCriteria = objectMapper.readValue(jsonString, InscriptionCriteria.class);
        assertNotNull(deserializedCriteria);
        assertEquals(criteria.getDateInscription(), deserializedCriteria.getDateInscription());
    }
}
