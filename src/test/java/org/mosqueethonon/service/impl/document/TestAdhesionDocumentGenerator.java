package org.mosqueethonon.service.impl.document;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class TestAdhesionDocumentGenerator {

    @InjectMocks
    private AdhesionDocumentGenerator generator;

    // -----------------------------------------------------------------------
    // getCode / getPath / getTemplateName / generateFileName / getIdUtilisateur
    // -----------------------------------------------------------------------

    @Test
    public void testGetCode() {
        assertEquals("ADHESION-001", generator.getCode());
    }

    @Test
    public void testGetPath() {
        assertEquals("ADHESION", generator.getPath());
    }

    @Test
    public void testGetTemplateName() {
        assertEquals("documents/adhesion-001", generator.getTemplateName());
    }

    @Test
    public void testGenerateFileName() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setId(42L);

        // WHEN
        String fileName = generator.generateFileName(entity);

        // THEN
        assertEquals("adhesion-42.pdf", fileName);
    }

    @Test
    public void testGetIdUtilisateurAlwaysReturnsNull() {
        // GIVEN
        AdhesionEntity entity = buildEntityComplete();

        // WHEN THEN
        assertNull(generator.getIdUtilisateur(entity));
    }

    // -----------------------------------------------------------------------
    // getAnnee
    // -----------------------------------------------------------------------

    @Test
    public void testGetAnneeWhenDateInscriptionIsNull() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setDateInscription(null);

        // WHEN
        String annee = generator.getAnnee(entity);

        // THEN
        assertNull(annee);
    }

    @Test
    public void testGetAnneeWhenDateInscriptionIsPresent() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setDateInscription(LocalDateTime.of(2025, 6, 15, 9, 0));

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
        AdhesionEntity entity = buildEntityComplete();

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals(12, variables.get("noMembre"));
        assertEquals("Dupont", variables.get("nom"));
        assertEquals("Marie", variables.get("prenom"));
        assertEquals("marie.dupont@example.com", variables.get("email"));
        assertEquals("0601020304", variables.get("mobile"));
        assertEquals("12 rue des Fleurs", variables.get("numeroEtRue"));
        assertEquals(74200, variables.get("codePostal"));
        assertEquals("Thonon-les-Bains", variables.get("ville"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — soussigneLibelle / etatCivil (titre)
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesTitreMasculin() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setTitre("M");

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals("Je soussigné", variables.get("soussigneLibelle"));
        assertEquals("Monsieur", variables.get("etatCivil"));
    }

    @Test
    public void testBuildTemplateVariablesTitreFeminin() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setTitre("F");

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals("Je soussignée", variables.get("soussigneLibelle"));
        assertEquals("Madame", variables.get("etatCivil"));
    }

    @Test
    public void testBuildTemplateVariablesTitreNull() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setTitre(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN — le comportement par défaut ("M".equals(null) == false) correspond au féminin
        assertEquals("Je soussignée", variables.get("soussigneLibelle"));
        assertEquals("Madame", variables.get("etatCivil"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — montant (montant vs montantAutre)
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesMontantUtiliseLeMontantSiPresent() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setMontant(new BigDecimal("50.00"));
        entity.setMontantAutre(new BigDecimal("30.00"));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals(new BigDecimal("50.00"), variables.get("montant"));
    }

    @Test
    public void testBuildTemplateVariablesMontantUtiliseMontantAutreEnFallback() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setMontant(null);
        entity.setMontantAutre(new BigDecimal("30.00"));

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertEquals(new BigDecimal("30.00"), variables.get("montant"));
    }

    @Test
    public void testBuildTemplateVariablesMontantNullQuandLesDeuxSontNull() {
        // GIVEN
        AdhesionEntity entity = buildEntityMinimal();
        entity.setMontant(null);
        entity.setMontantAutre(null);

        // WHEN
        Map<String, Object> variables = generator.buildTemplateVariables(entity);

        // THEN
        assertNull(variables.get("montant"));
    }

    // -----------------------------------------------------------------------
    // computeHash
    // -----------------------------------------------------------------------

    @Test
    public void testComputeHashDeterministe() {
        // GIVEN
        AdhesionEntity entity = buildEntityComplete();

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
        AdhesionEntity entity1 = buildEntityComplete();
        AdhesionEntity entity2 = buildEntityComplete();
        entity2.setNom("Autre Nom");

        // WHEN
        String hash1 = generator.computeHash(entity1);
        String hash2 = generator.computeHash(entity2);

        // THEN
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testComputeHashDifferentQuandMontantFallbackChange() {
        // GIVEN — hash différent selon la valeur retenue par le fallback montant/montantAutre
        AdhesionEntity entity1 = buildEntityMinimal();
        entity1.setMontant(null);
        entity1.setMontantAutre(new BigDecimal("30.00"));

        AdhesionEntity entity2 = buildEntityMinimal();
        entity2.setMontant(null);
        entity2.setMontantAutre(new BigDecimal("45.00"));

        // WHEN
        String hash1 = generator.computeHash(entity1);
        String hash2 = generator.computeHash(entity2);

        // THEN
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testComputeHashNeLevePasExceptionQuandChampsNullables() {
        // GIVEN
        AdhesionEntity entity = new AdhesionEntity();
        entity.setId(1L);

        // WHEN THEN — ne doit pas lever d'exception
        String hash = generator.computeHash(entity);
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // buildMetadata
    // -----------------------------------------------------------------------

    @Test
    public void testBuildMetadataContientIdNomPrenomEmail() {
        // GIVEN
        AdhesionEntity entity = buildEntityComplete();

        // WHEN
        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);

        // THEN
        assertEquals(4, metadata.size());
        assertTrue(metadata.stream().anyMatch(m -> "5".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "Dupont".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "Marie".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "marie.dupont@example.com".equals(m.getValeur())));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private AdhesionEntity buildEntityMinimal() {
        AdhesionEntity entity = new AdhesionEntity();
        entity.setId(1L);
        return entity;
    }

    private AdhesionEntity buildEntityComplete() {
        AdhesionEntity entity = new AdhesionEntity();
        entity.setId(5L);
        entity.setTitre("M");
        entity.setNom("Dupont");
        entity.setPrenom("Marie");
        entity.setEmail("marie.dupont@example.com");
        entity.setMobile("0601020304");
        entity.setNumeroEtRue("12 rue des Fleurs");
        entity.setCodePostal(74200);
        entity.setVille("Thonon-les-Bains");
        entity.setMontant(new BigDecimal("50.00"));
        entity.setMontantAutre(new BigDecimal("30.00"));
        entity.setNoMembre(12);
        entity.setDateInscription(LocalDateTime.of(2025, 6, 15, 9, 0));
        return entity;
    }

}
