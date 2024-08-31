package org.mosqueethonon.v1.dto;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInscriptionEnfantDto {

    @Test
    public void testNormalize() {
        // GIVEN
        ResponsableLegalDto responsableLegalDto = ResponsableLegalDto.builder().nom(" eL yahyaouI ").prenom("shAms edDine ")
                .nomAutre("eL yahyaouI ").prenomAutre("shAms edDine ").build();
        EleveDto eleve = EleveDto.builder().nom(" eL yahyaouI ").prenom("shAms edDine ").build();
        InscriptionEnfantDto inscriptionEnfantDto = InscriptionEnfantDto.builder().responsableLegal(responsableLegalDto).eleves(Lists.newArrayList(eleve)).build();

        // WHEN
        inscriptionEnfantDto.normalize();

        // THEN
        assertEquals("El Yahyaoui", responsableLegalDto.getNom());
        assertEquals("El Yahyaoui", responsableLegalDto.getNomAutre());
        assertEquals("El Yahyaoui", eleve.getNom());
        assertEquals("Shams Eddine", responsableLegalDto.getPrenom());
        assertEquals("Shams Eddine", responsableLegalDto.getPrenomAutre());
        assertEquals("Shams Eddine", eleve.getPrenom());
    }
}
