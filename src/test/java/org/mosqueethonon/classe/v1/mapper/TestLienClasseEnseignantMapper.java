package org.mosqueethonon.classe.v1.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.classe.entity.LienClasseEnseignantEntity;
import org.mosqueethonon.classe.v1.dto.EnseignantDto;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;

public class TestLienClasseEnseignantMapper {

    private final LienClasseEnseignantMapper underTest = new LienClasseEnseignantMapperImpl();

    private LienClasseEnseignantEntity lienAvecEnseignant(String prenom, String nom) {
        UtilisateurEntity enseignant = new UtilisateurEntity();
        enseignant.setId(7L);
        enseignant.setPrenom(prenom);
        enseignant.setNom(nom);
        return LienClasseEnseignantEntity.builder().idUtilisateur(7L).enseignant(enseignant).build();
    }

    @Test
    public void testConcateneLePrenomPuisLeNom() {
        assertEquals("Jean Dupont", underTest.concatNomPrenomEnseignant(lienAvecEnseignant("Jean", "Dupont")));
    }

    @Test
    public void testAucunEnseignantRattache() {
        LienClasseEnseignantEntity lien = LienClasseEnseignantEntity.builder().idUtilisateur(7L).build();

        assertNull(underTest.concatNomPrenomEnseignant(lien));
    }

    @Test
    public void testNiNomNiPrenom() {
        assertNull(underTest.concatNomPrenomEnseignant(lienAvecEnseignant(null, null)));
    }

    @Test
    public void testSeulementLeNom() {
        assertEquals("Dupont", underTest.concatNomPrenomEnseignant(lienAvecEnseignant(null, "Dupont")));
    }

    @Test
    public void testSeulementLePrenom() {
        assertEquals("Jean", underTest.concatNomPrenomEnseignant(lienAvecEnseignant("Jean", null)));
    }

    @Test
    public void testMappeLIdUtilisateurVersLIdDuDto() {
        EnseignantDto dto = underTest.fromEntityToDto(lienAvecEnseignant("Jean", "Dupont"));

        assertEquals(7L, dto.getId());
        assertEquals("Jean Dupont", dto.getNomPrenom());
    }

    @Test
    public void testMappeLIdDuDtoVersLIdUtilisateur() {
        LienClasseEnseignantEntity entity = underTest.fromDtoToEntity(
                EnseignantDto.builder().id(7L).nomPrenom("Jean Dupont").build());

        assertEquals(7L, entity.getIdUtilisateur());
        assertNull(entity.getId());
        assertNull(entity.getIdClasse());
    }
}
