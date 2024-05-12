package org.mosqueethonon.v1.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestAdhesionDto {

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

}
