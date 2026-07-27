package org.mosqueethonon.inscription.v1.dto;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.inscription.v1.dto.EleveDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.inscription.v1.dto.ResponsableLegalDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInscriptionEnfantDto {

    @Test
    public void testNormalize() {
        // GIVEN
        ResponsableLegalDto responsableLegalDto = ResponsableLegalDto.builder().nom(" eL yahyaouI ").prenom("shAms edDine ")
                .nomAutre("eL yahyaouI ").prenomAutre("shAms edDine ").email(" ShamSEddIne@Gmail.Com ").build();
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
        assertEquals("shamseddine@gmail.com", responsableLegalDto.getEmail());
        assertEquals("Shams Eddine", eleve.getPrenom());
    }
}
