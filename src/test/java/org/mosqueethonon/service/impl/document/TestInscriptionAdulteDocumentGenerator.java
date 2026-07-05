package org.mosqueethonon.service.impl.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionMatiereEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionAdulteDocumentGenerator {

    @Mock
    private TraductionService traductionService;

    @InjectMocks
    private InscriptionAdulteDocumentGenerator generator;

    // -----------------------------------------------------------------------
    // getCode / getPath / getTemplateName / generateFileName / getIdUtilisateur
    // -----------------------------------------------------------------------

    @Test
    public void testGetCode() {
        assertEquals("INS-ADULTE-001", generator.getCode());
    }

    @Test
    public void testGetPath() {
        assertEquals("INSCRIPTION-ADULTE", generator.getPath());
    }

    @Test
    public void testGetTemplateName() {
        assertEquals("documents/ins-adulte-001", generator.getTemplateName());
    }

    @Test
    public void testGenerateFileName() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setNoInscription("2025-0002");

        // WHEN
        String fileName = generator.generateFileName(entity);

        // THEN
        assertEquals("inscription-adulte-2025-0002.pdf", fileName);
    }

    @Test
    public void testGetIdUtilisateur() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setIdUtilisateur(55L);

        // WHEN THEN
        assertEquals(55L, generator.getIdUtilisateur(entity));
    }

    // -----------------------------------------------------------------------
    // getAnnee
    // -----------------------------------------------------------------------

    @Test
    public void testGetAnneeWhenDateInscriptionIsPresent() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setDateInscription(LocalDateTime.of(2025, 9, 1, 10, 0));

        // WHEN
        String annee = generator.getAnnee(entity);

        // THEN
        assertEquals("2025", annee);
    }

    @Test
    public void testGetAnneeWhenDateInscriptionIsNullThrows() {
        // GIVEN — comportement actuel : pas de garde contre le null, contrairement aux
        // autres générateurs. On documente ce comportement plutôt que de le corriger.
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setDateInscription(null);

        // WHEN THEN
        assertThrows(NullPointerException.class, () -> generator.getAnnee(entity));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — champs simples
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesChampsSimples() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setNoInscription("2025-0002");
        entity.setStatut(StatutInscription.VALIDEE);
        LocalDateTime dateInscription = LocalDateTime.of(2025, 9, 1, 10, 0);
        entity.setDateInscription(dateInscription);
        entity.setMontantTotal(new BigDecimal("80.00"));
        entity.setStatutProfessionnel(StatutProfessionnelEnum.ETUDIANT);
        entity.setAnneeScolaire("2025-2026");

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals("2025-0002", variables.get("noInscription"));
        assertEquals("VALIDEE", variables.get("statut"));
        assertEquals(dateInscription, variables.get("dateInscription"));
        assertEquals(new BigDecimal("80.00"), variables.get("montantTotal"));
        assertEquals("ETUDIANT", variables.get("statutProfessionnel"));
        assertEquals("2025-2026", variables.get("anneeScolaire"));
    }

    @Test
    public void testBuildTemplateVariablesStatutEtStatutProfessionnelNull() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setStatut(null);
        entity.setStatutProfessionnel(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertNull(variables.get("statut"));
        assertNull(variables.get("statutProfessionnel"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — responsableLegal
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesResponsableLegalPresent() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(buildResponsableLegal());

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
    }

    @Test
    public void testBuildTemplateVariablesResponsableLegalNull() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertFalse(variables.containsKey("nom"));
        assertFalse(variables.containsKey("email"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — eleves (seul le premier élève est utilisé)
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesElevesNull() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setEleves(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertFalse(variables.containsKey("dateNaissance"));
        assertFalse(variables.containsKey("sexe"));
        assertFalse(variables.containsKey("niveauInterne"));
    }

    @Test
    public void testBuildTemplateVariablesElevesVide() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setEleves(new ArrayList<>());

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertFalse(variables.containsKey("dateNaissance"));
    }

    @Test
    public void testBuildTemplateVariablesUtiliseSeulementLePremierEleve() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        LocalDate dateNaissance1 = LocalDate.of(1990, 1, 1);
        EleveEntity eleve1 = buildEleve(dateNaissance1, SexeEnum.F, NiveauInterneEnum.DEBUTANT);
        EleveEntity eleve2 = buildEleve(LocalDate.of(1985, 5, 5), SexeEnum.M, NiveauInterneEnum.AVANCE);
        entity.setEleves(List.of(eleve1, eleve2));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals(dateNaissance1, variables.get("dateNaissance"));
        assertEquals("F", variables.get("sexe"));
        assertEquals("DEBUTANT", variables.get("niveauInterne"));
    }

    @Test
    public void testBuildTemplateVariablesEleveSexeEtNiveauInterneNull() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        EleveEntity eleve = buildEleve(LocalDate.of(1990, 1, 1), null, null);
        entity.setEleves(List.of(eleve));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertNull(variables.get("sexe"));
        assertNull(variables.get("niveauInterne"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — matieres
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesMatieresNull() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setMatieres(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertFalse(variables.containsKey("matieres"));
    }

    @Test
    public void testBuildTemplateVariablesMatieresAvecTraductionTrouvee() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        InscriptionMatiereEntity matiereFiqh = buildInscriptionMatiere(MatiereEnum.FIQH);
        entity.setMatieres(List.of(matiereFiqh));
        when(traductionService.findTraductionByCleAndValeur("cdmaticode", "FIQH"))
                .thenReturn(TraductionDto.builder().fr("Jurisprudence").build());

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<String> matieres = (List<String>) variables.get("matieres");
        assertEquals(1, matieres.size());
        assertEquals("Jurisprudence", matieres.get(0));
    }

    @Test
    public void testBuildTemplateVariablesMatieresUtiliseCodeBrutSiTraductionIntrouvable() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        InscriptionMatiereEntity matiereLangueArabe = buildInscriptionMatiere(MatiereEnum.LANGUE_ARABE);
        entity.setMatieres(List.of(matiereLangueArabe));
        when(traductionService.findTraductionByCleAndValeur("cdmaticode", "LANGUE_ARABE"))
                .thenThrow(new ResourceNotFoundException("Traduction non trouvée"));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<String> matieres = (List<String>) variables.get("matieres");
        assertEquals(1, matieres.size());
        assertEquals("LANGUE_ARABE", matieres.get(0));
    }

    @Test
    public void testBuildTemplateVariablesMatieresVide() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setMatieres(new ArrayList<>());

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        @SuppressWarnings("unchecked")
        List<String> matieres = (List<String>) variables.get("matieres");
        assertNotNull(matieres);
        assertTrue(matieres.isEmpty());
    }

    // -----------------------------------------------------------------------
    // computeHash
    // -----------------------------------------------------------------------

    @Test
    public void testComputeHashDeterministe() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityComplete();

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
        InscriptionAdulteEntity entity1 = buildEntityComplete();
        InscriptionAdulteEntity entity2 = buildEntityComplete();
        entity2.setMontantTotal(new BigDecimal("999.99"));

        // WHEN
        String hash1 = generator.computeHash(entity1);
        String hash2 = generator.computeHash(entity2);

        // THEN
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testComputeHashNeLevePasExceptionQuandChampsNullables() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(null);
        entity.setEleves(null);
        entity.setMatieres(null);

        // WHEN THEN — ne doit pas lever d'exception, et n'appelle pas traductionService
        String hash = generator.computeHash(entity);
        assertNotNull(hash);
        verifyNoInteractions(traductionService);
    }

    @Test
    public void testComputeHashNAppellePasTraductionServicePourMatieres() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setMatieres(List.of(buildInscriptionMatiere(MatiereEnum.FIQH)));

        // WHEN
        generator.computeHash(entity);

        // THEN — le hash se base sur le code brut, pas sur la traduction
        verifyNoInteractions(traductionService);
    }

    // -----------------------------------------------------------------------
    // buildMetadata
    // -----------------------------------------------------------------------

    @Test
    public void testBuildMetadataChampsObligatoires() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setId(42L);
        entity.setNoInscription("2025-0002");
        entity.setResponsableLegal(null);
        entity.setIdUtilisateur(null);
        entity.setAnneeScolaire(null);

        // WHEN
        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);

        // THEN
        assertEquals(2, metadata.size());
        assertTrue(metadata.stream().anyMatch(m -> "42".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "2025-0002".equals(m.getValeur())));
    }

    @Test
    public void testBuildMetadataAvecResponsableLegalIdUtilisateurEtAnneeScolaire() {
        // GIVEN
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setResponsableLegal(buildResponsableLegal());
        entity.setIdUtilisateur(7L);
        entity.setAnneeScolaire("2025-2026");

        // WHEN
        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);

        // THEN
        assertTrue(metadata.stream().anyMatch(m -> "Dupont".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "Marie".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "marie.dupont@example.com".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "7".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "2025-2026".equals(m.getValeur())));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private InscriptionAdulteEntity buildEntityMinimal() {
        InscriptionAdulteEntity entity = new InscriptionAdulteEntity();
        entity.setId(1L);
        entity.setNoInscription("2025-0002");
        entity.setDateInscription(LocalDateTime.of(2025, 9, 1, 10, 0));
        return entity;
    }

    private InscriptionAdulteEntity buildEntityComplete() {
        InscriptionAdulteEntity entity = buildEntityMinimal();
        entity.setStatut(StatutInscription.VALIDEE);
        entity.setMontantTotal(new BigDecimal("80.00"));
        entity.setStatutProfessionnel(StatutProfessionnelEnum.ETUDIANT);
        entity.setAnneeScolaire("2025-2026");
        entity.setResponsableLegal(buildResponsableLegal());
        entity.setEleves(List.of(buildEleve(LocalDate.of(1990, 1, 1), SexeEnum.F, NiveauInterneEnum.DEBUTANT)));
        entity.setMatieres(List.of(buildInscriptionMatiere(MatiereEnum.FIQH)));
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
        return resp;
    }

    private EleveEntity buildEleve(LocalDate dateNaissance, SexeEnum sexe, NiveauInterneEnum niveauInterne) {
        EleveEntity eleve = new EleveEntity();
        eleve.setDateNaissance(dateNaissance);
        eleve.setSexe(sexe);
        eleve.setNiveauInterne(niveauInterne);
        return eleve;
    }

    private InscriptionMatiereEntity buildInscriptionMatiere(MatiereEnum code) {
        MatiereEntity matiereEntity = new MatiereEntity();
        matiereEntity.setCode(code);
        InscriptionMatiereEntity inscriptionMatiere = new InscriptionMatiereEntity();
        inscriptionMatiere.setMatiere(matiereEntity);
        return inscriptionMatiere;
    }

}
