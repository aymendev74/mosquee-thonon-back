package org.mosqueethonon.v1.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionLightDto;

import java.time.LocalDateTime;

public class TestAdhesionLightDto {

    @Test
    public void testNormalizeNotNormalizedString() {
        // GIVEN
        AdhesionDto adhesionDto = AdhesionDto.builder().nom(" eL yahyaouI ").prenom(" shAms edDine ").build();

        // WHEN
        adhesionDto.normalize();

        // THEN
        assertEquals("El Yahyaoui", adhesionDto.getNom());
        assertEquals("Shams Eddine", adhesionDto.getPrenom());
    }

    @Test
    public void testNormalizeAlreadyNormalizedString() {
        // GIVEN
        AdhesionDto adhesionDto = AdhesionDto.builder().nom("El Yahyaoui").prenom("Shams Eddine").build();

        // WHEN
        adhesionDto.normalize();

        // THEN
        assertEquals("El Yahyaoui", adhesionDto.getNom());
        assertEquals("Shams Eddine", adhesionDto.getPrenom());
    }

    @Test
    public void testSerializeDeserialize() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        AdhesionLightDto adhesionDto = new AdhesionLightDto();
        adhesionDto.setDateInscription(LocalDateTime.of(2024, 5, 17, 6, 15));

        // Serialization
        String jsonString = objectMapper.writeValueAsString(adhesionDto);
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("17.05.2024 06:15:00.000"));

        // Deserialization
        AdhesionLightDto deserializedAdhesion = objectMapper.readValue(jsonString, AdhesionLightDto.class);
        assertNotNull(deserializedAdhesion);
        assertEquals(adhesionDto.getDateInscription(), deserializedAdhesion.getDateInscription());
    }
}
