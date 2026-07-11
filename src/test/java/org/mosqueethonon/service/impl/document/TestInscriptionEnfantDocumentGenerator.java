package org.mosqueethonon.service.impl.document;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionEnfantDocumentGenerator {

    @InjectMocks
    private InscriptionEnfantDocumentGenerator generator;

    // -----------------------------------------------------------------------
    // getCode / getPath / getTemplateName / generateFileName / getIdUtilisateur
    // -----------------------------------------------------------------------

    @Test
    public void testGetCode() {
        assertEquals("INS-ENFANT-001", generator.getCode());
    }

    @Test
    public void testGetPath() {
        assertEquals("INSCRIPTION-ENFANT", generator.getPath());
    }

    @Test
    public void testGetTemplateName() {
        assertEquals("documents/ins-enfant-001", generator.getTemplateName());
    }

    @Test
    public void testGenerateFileName() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setNoInscription("2025-0001");

        // WHEN
        String fileName = generator.generateFileName(entity);

        // THEN
        assertEquals("inscription-enfant-2025-0001.pdf", fileName);
    }

    @Test
    public void testGetIdUtilisateur() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setIdUtilisateur(99L);

        // WHEN THEN
        assertEquals(99L, generator.getIdUtilisateur(entity));
    }

    @Test
    public void testGetIdUtilisateurWhenNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();

        // WHEN THEN
        assertNull(generator.getIdUtilisateur(entity));
    }

    // -----------------------------------------------------------------------
    // getAnnee
    // -----------------------------------------------------------------------

    @Test
    public void testGetAnneeWhenDateInscriptionIsNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setDateInscription(null);

        // WHEN
        String annee = generator.getAnnee(entity);

        // THEN
        assertNull(annee);
    }

    @Test
    public void testGetAnneeWhenDateInscriptionIsPresent() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setDateInscription(LocalDateTime.of(2025, 9, 1, 10, 0));

        // WHEN
        String annee = generator.getAnnee(entity);

        // THEN
        assertEquals("2025", annee);
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — champs simples
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesChampsSimples() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setNoInscription("2025-0001");
        entity.setAnneeScolaire("2025-2026");
        entity.setMontantTotal(new BigDecimal("120.50"));
        LocalDateTime dateInscription = LocalDateTime.of(2025, 9, 1, 10, 0);
        entity.setDateInscription(dateInscription);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals("2025-0001", variables.get("noInscription"));
        assertEquals("2025-2026", variables.get("anneeScolaire"));
        assertEquals(new BigDecimal("120.50"), variables.get("montantTotal"));
        assertEquals(dateInscription, variables.get("dateInscription"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — responsableLegal
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesResponsableLegalPresent() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        ResponsableLegalEntity resp = buildResponsableLegal();
        entity.setResponsableLegal(resp);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals("Dupont", variables.get("nom"));
        assertEquals("Marie", variables.get("prenom"));
        assertEquals("marie.dupont@example.com", variables.get("email"));
        assertEquals("0601020304", variables.get("mobile"));
        assertEquals("12 rue des Fleurs", variables.get("numeroEtRue"));
        assertEquals(74200, variables.get("codePostal"));
        assertEquals("Thonon-les-Bains", variables.get("ville"));
        assertEquals(Boolean.TRUE, variables.get("adherent"));
        assertEquals("Martin", variables.get("nomAutre"));
        assertEquals("Paul", variables.get("prenomAutre"));
        assertEquals("Père", variables.get("lienParente"));
        assertEquals("0605060708", variables.get("telephoneAutre"));
        assertEquals(Boolean.TRUE, variables.get("autorisationAutonomie"));
        assertEquals(Boolean.FALSE, variables.get("autorisationMedia"));
    }

    @Test
    public void testBuildTemplateVariablesResponsableLegalNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertFalse(variables.containsKey("nom"));
        assertFalse(variables.containsKey("prenom"));
        assertFalse(variables.containsKey("email"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — eleves
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesElevesNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setEleves(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertFalse(variables.containsKey("eleves"));
    }

    @Test
    public void testBuildTemplateVariablesElevesVide() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setEleves(new ArrayList<>());

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertNotNull(eleves);
        assertTrue(eleves.isEmpty());
    }

    @Test
    public void testBuildTemplateVariablesElevesTrieesParNomPrenom() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        EleveEntity zidane = buildEleve("Zidane", "Adam", LocalDate.of(2015, 1, 1), NiveauScolaireEnum.CP, NiveauInterneEnum.P1);
        EleveEntity aziz = buildEleve("Aziz", "Yasmine", LocalDate.of(2013, 5, 5), NiveauScolaireEnum.CE1, NiveauInterneEnum.P2);
        entity.setEleves(List.of(zidane, aziz));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertEquals(2, eleves.size());
        assertEquals("Aziz", eleves.get(0).get("nom"));
        assertEquals("Zidane", eleves.get(1).get("nom"));
    }

    @Test
    public void testBuildTemplateVariablesEleveDateNaissanceNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        EleveEntity eleve = buildEleve("Dupont", "Marie", null, NiveauScolaireEnum.CP, NiveauInterneEnum.P1);
        entity.setEleves(List.of(eleve));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertEquals("", eleves.get(0).get("dateNaissance"));
    }

    @Test
    public void testBuildTemplateVariablesEleveDateNaissancePresente() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        LocalDate dateNaissance = LocalDate.of(2015, 3, 20);
        EleveEntity eleve = buildEleve("Dupont", "Marie", dateNaissance, NiveauScolaireEnum.CP, NiveauInterneEnum.P1);
        entity.setEleves(List.of(eleve));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertEquals(dateNaissance.toString(), eleves.get(0).get("dateNaissance"));
    }

    @Test
    public void testBuildTemplateVariablesEleveNiveauInterneNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        EleveEntity eleve = buildEleve("Dupont", "Marie", null, NiveauScolaireEnum.CP, null);
        entity.setEleves(List.of(eleve));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertEquals("", eleves.get(0).get("niveauInterne"));
    }

    @Test
    public void testBuildTemplateVariablesEleveNiveauInternePresent() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        EleveEntity eleve = buildEleve("Dupont", "Marie", null, NiveauScolaireEnum.CP, NiveauInterneEnum.N2_1);
        entity.setEleves(List.of(eleve));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertEquals("N2_1", eleves.get(0).get("niveauInterne"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — getLibelleNiveauScolaire (toutes les branches)
    // -----------------------------------------------------------------------

    @Test
    public void testLibelleNiveauScolaireToutesLesBranches() {
        assertLibelleNiveau(NiveauScolaireEnum.CP, "CP");
        assertLibelleNiveau(NiveauScolaireEnum.CE1, "CE1");
        assertLibelleNiveau(NiveauScolaireEnum.CE2, "CE2");
        assertLibelleNiveau(NiveauScolaireEnum.CM1, "CM1");
        assertLibelleNiveau(NiveauScolaireEnum.CM2, "CM2");
        assertLibelleNiveau(NiveauScolaireEnum.COLLEGE_6EME, "6ème");
        assertLibelleNiveau(NiveauScolaireEnum.COLLEGE_5EME, "5ème");
        assertLibelleNiveau(NiveauScolaireEnum.COLLEGE_4EME, "4ème");
        assertLibelleNiveau(NiveauScolaireEnum.COLLEGE_3EME, "3ème");
        assertLibelleNiveau(NiveauScolaireEnum.LYCEE_2ND, "2nd");
        assertLibelleNiveau(NiveauScolaireEnum.LYCEE_1ERE, "1ère");
        assertLibelleNiveau(NiveauScolaireEnum.LYCEE_TERM, "Terminal");
        assertLibelleNiveau(NiveauScolaireEnum.AUTRE, "Autre");
    }

    @Test
    public void testLibelleNiveauScolaireNull() {
        assertLibelleNiveau(null, "");
    }

    private void assertLibelleNiveau(NiveauScolaireEnum niveau, String libelleAttendu) {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        EleveEntity eleve = buildEleve("Dupont", "Marie", null, niveau, null);
        entity.setEleves(List.of(eleve));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> eleves = (List<Map<String, String>>) variables.get("eleves");
        assertEquals(libelleAttendu, eleves.get(0).get("niveau"));
    }

    // -----------------------------------------------------------------------
    // computeHash
    // -----------------------------------------------------------------------

    @Test
    public void testComputeHashDeterministe() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityComplete();

        // WHEN
        String hash1 = generator.computeHash(entity);
        String hash2 = generator.computeHash(entity);

        // THEN
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    public void testComputeHashDifferentQuandChampChange() {
        // GIVEN
        InscriptionEnfantEntity entity1 = buildEntityComplete();
        InscriptionEnfantEntity entity2 = buildEntityComplete();
        entity2.setMontantTotal(new BigDecimal("999.99"));

        // WHEN
        String hash1 = generator.computeHash(entity1);
        String hash2 = generator.computeHash(entity2);

        // THEN
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testComputeHashNeLevePasExceptionQuandResponsableEtElevesNull() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(null);
        entity.setEleves(null);

        // WHEN THEN — ne doit pas lever d'exception
        String hash = generator.computeHash(entity);
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // buildMetadata
    // -----------------------------------------------------------------------

    @Test
    public void testBuildMetadataChampsObligatoires() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setId(42L);
        entity.setNoInscription("2025-0001");
        entity.setResponsableLegal(null);
        entity.setIdUtilisateur(null);
        entity.setAnneeScolaire(null);

        // WHEN
        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);

        // THEN
        assertEquals(2, metadata.size());
        assertTrue(metadata.stream().anyMatch(m -> "42".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "2025-0001".equals(m.getValeur())));
    }

    @Test
    public void testBuildMetadataAvecResponsableLegal() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(buildResponsableLegal());

        // WHEN
        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);

        // THEN
        assertTrue(metadata.stream().anyMatch(m -> "Dupont".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "Marie".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "marie.dupont@example.com".equals(m.getValeur())));
    }

    @Test
    public void testBuildMetadataAvecIdUtilisateurEtAnneeScolaire() {
        // GIVEN
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setIdUtilisateur(7L);
        entity.setAnneeScolaire("2025-2026");

        // WHEN
        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);

        // THEN
        assertTrue(metadata.stream().anyMatch(m -> "7".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "2025-2026".equals(m.getValeur())));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private InscriptionEnfantEntity buildEntityMinimal() {
        InscriptionEnfantEntity entity = new InscriptionEnfantEntity();
        entity.setId(1L);
        entity.setNoInscription("2025-0001");
        return entity;
    }

    private InscriptionEnfantEntity buildEntityComplete() {
        InscriptionEnfantEntity entity = buildEntityMinimal();
        entity.setMontantTotal(new BigDecimal("120.50"));
        entity.setAnneeScolaire("2025-2026");
        entity.setResponsableLegal(buildResponsableLegal());
        entity.setEleves(List.of(buildEleve("Dupont", "Marie", LocalDate.of(2015, 3, 20), NiveauScolaireEnum.CP, NiveauInterneEnum.P1)));
        return entity;
    }

    private ResponsableLegalEntity buildResponsableLegal() {
        ResponsableLegalEntity resp = new ResponsableLegalEntity();
        resp.setNom("Dupont");
        resp.setPrenom("Marie");
        resp.setEmail("marie.dupont@example.com");
        resp.setMobile("0601020304");
        resp.setNumeroEtRue("12 rue des Fleurs");
        resp.setCodePostal(74200);
        resp.setVille("Thonon-les-Bains");
        resp.setAdherent(Boolean.TRUE);
        resp.setNomAutre("Martin");
        resp.setPrenomAutre("Paul");
        resp.setLienParente("Père");
        resp.setTelephoneAutre("0605060708");
        resp.setAutorisationAutonomie(Boolean.TRUE);
        resp.setAutorisationMedia(Boolean.FALSE);
        return resp;
    }

    private EleveEntity buildEleve(String nom, String prenom, LocalDate dateNaissance, NiveauScolaireEnum niveau, NiveauInterneEnum niveauInterne) {
        EleveEntity eleve = new EleveEntity();
        eleve.setNom(nom);
        eleve.setPrenom(prenom);
        eleve.setDateNaissance(dateNaissance);
        eleve.setNiveau(niveau);
        eleve.setNiveauInterne(niveauInterne);
        return eleve;
    }

}
