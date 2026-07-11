package org.mosqueethonon.service.impl.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.classe.LienClasseEnseignantEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NoteMatiereEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestBulletinDocumentGenerator {

    @Mock
    private EleveRepository eleveRepository;

    @Mock
    private ClasseRepository classeRepository;

    @Mock
    private TraductionService traductionService;

    @InjectMocks
    private BulletinDocumentGenerator bulletinDocumentGenerator;

    // -----------------------------------------------------------------------
    // getCode / getTemplateName / generateFileName
    // -----------------------------------------------------------------------

    @Test
    public void testGetCode() {
        assertEquals("BULLETIN-001", bulletinDocumentGenerator.getCode());
    }

    @Test
    public void testGetTemplateName() {
        assertEquals("documents/bulletin-001", bulletinDocumentGenerator.getTemplateName());
    }

    @Test
    public void testGenerateFileName() {
        // GIVEN
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setId(42L);

        // WHEN
        String fileName = bulletinDocumentGenerator.generateFileName(bulletin);

        // THEN
        assertEquals("bulletin-42.pdf", fileName);
    }

    @Test
    public void testGetAnneeWhenAnneeIsNull() {
        // GIVEN
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setAnnee(null);

        // WHEN
        String annee = bulletinDocumentGenerator.getAnnee(bulletin);

        // THEN
        assertNull(annee);
    }

    @Test
    public void testGetAnneeWhenAnneeIsPresent() {
        // GIVEN
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setAnnee(2025);

        // WHEN
        String annee = bulletinDocumentGenerator.getAnnee(bulletin);

        // THEN
        assertEquals("2025", annee);
    }

    @Test
    public void testGetIdUtilisateurReturnsNull() {
        // GIVEN
        BulletinEntity bulletin = new BulletinEntity();

        // WHEN
        Long idUtilisateur = bulletinDocumentGenerator.getIdUtilisateur(bulletin);

        // THEN
        assertNull(idUtilisateur);
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — nomPrenomEleve
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesNomPrenomEleve() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("Dupont Marie", variables.get("nomPrenomEleve"));
    }

    @Test
    public void testBuildTemplateVariablesThrowsWhenEleveNotFound() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 99L);
        when(eleveRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN THEN
        assertThrows(ResourceNotFoundException.class,
                () -> bulletinDocumentGenerator.buildTemplateVariables(bulletin));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — moisAnnee
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesMoisAnneeAvecMoisEtAnnee() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setMois(3);   // Mars
        bulletin.setAnnee(2025);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("Mars 2025", variables.get("moisAnnee"));
    }

    @Test
    public void testBuildTemplateVariablesMoisAnneeWhenMoisIsNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setMois(null);
        bulletin.setAnnee(2025);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("moisAnnee"));
    }

    @Test
    public void testBuildTemplateVariablesMoisAnneeWhenAnneeIsNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setMois(5);
        bulletin.setAnnee(null);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("moisAnnee"));
    }

    @Test
    public void testBuildTemplateVariablesMoisBornesValidesJanvierDecembre() {
        // GIVEN — Janvier (mois=1)
        BulletinEntity bulletinJanvier = buildBulletinMinimal(1L, 10L);
        bulletinJanvier.setMois(1);
        bulletinJanvier.setAnnee(2025);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        Map<String, Object> variablesJanvier = bulletinDocumentGenerator.buildTemplateVariables(bulletinJanvier);
        assertEquals("Janvier 2025", variablesJanvier.get("moisAnnee"));

        // GIVEN — Décembre (mois=12)
        reset(eleveRepository);
        BulletinEntity bulletinDecembre = buildBulletinMinimal(2L, 10L);
        bulletinDecembre.setMois(12);
        bulletinDecembre.setAnnee(2025);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        Map<String, Object> variablesDecembre = bulletinDocumentGenerator.buildTemplateVariables(bulletinDecembre);
        assertEquals("Décembre 2025", variablesDecembre.get("moisAnnee"));
    }

    @Test
    public void testBuildTemplateVariablesMoisHorsLimiteRetourneVide() {
        // GIVEN — mois=0 (hors bornes)
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setMois(0);
        bulletin.setAnnee(2025);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN — nomMois vide quand index invalide
        assertEquals(" 2025", variables.get("moisAnnee"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — nomsEnseignants
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesNomsEnseignantsAvecDeuxEnseignants() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", 5L);

        UtilisateurEntity ens1 = buildUtilisateur("Martin", "Pierre");
        UtilisateurEntity ens2 = buildUtilisateur("Roux", "Sophie");
        LienClasseEnseignantEntity lien1 = new LienClasseEnseignantEntity();
        lien1.setEnseignant(ens1);
        LienClasseEnseignantEntity lien2 = new LienClasseEnseignantEntity();
        lien2.setEnseignant(ens2);

        ClasseEntity classe = new ClasseEntity();
        classe.setLiensClasseEnseignants(List.of(lien1, lien2));

        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));
        when(classeRepository.findById(5L)).thenReturn(Optional.of(classe));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("Martin Pierre, Roux Sophie", variables.get("nomsEnseignants"));
    }

    @Test
    public void testBuildTemplateVariablesNomsEnseignantsWhenClasseIdIsNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("nomsEnseignants"));
        verify(classeRepository, never()).findById(any());
    }

    @Test
    public void testBuildTemplateVariablesNomsEnseignantsWhenClasseNotFound() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", 5L);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));
        when(classeRepository.findById(5L)).thenReturn(Optional.empty());

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("nomsEnseignants"));
    }

    @Test
    public void testBuildTemplateVariablesNomsEnseignantsWhenLiensIsNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", 5L);
        ClasseEntity classe = new ClasseEntity();
        classe.setLiensClasseEnseignants(null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));
        when(classeRepository.findById(5L)).thenReturn(Optional.of(classe));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("nomsEnseignants"));
    }

    @Test
    public void testBuildTemplateVariablesNomsEnseignantsIgnoreLienSansEnseignant() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", 5L);

        LienClasseEnseignantEntity lienAvecEnseignant = new LienClasseEnseignantEntity();
        lienAvecEnseignant.setEnseignant(buildUtilisateur("Ben Ali", "Youssef"));
        LienClasseEnseignantEntity lienSansEnseignant = new LienClasseEnseignantEntity();
        lienSansEnseignant.setEnseignant(null);

        ClasseEntity classe = new ClasseEntity();
        classe.setLiensClasseEnseignants(List.of(lienAvecEnseignant, lienSansEnseignant));

        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));
        when(classeRepository.findById(5L)).thenReturn(Optional.of(classe));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("Ben Ali Youssef", variables.get("nomsEnseignants"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — lignesMatieres
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesLignesMatieresAvecDeuxMatieres() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setMois(5);
        bulletin.setAnnee(2025);

        MatiereEntity matiereCoran = buildMatiere(MatiereEnum.CORAN);
        BulletinMatiereEntity bmCoran = new BulletinMatiereEntity();
        bmCoran.setMatiere(matiereCoran);
        bmCoran.setNote(NoteMatiereEnum.A);
        bmCoran.setRemarque("Très bien");

        MatiereEntity matiereEcriture = buildMatiere(MatiereEnum.ECRITURE);
        BulletinMatiereEntity bmEcriture = new BulletinMatiereEntity();
        bmEcriture.setMatiere(matiereEcriture);
        bmEcriture.setNote(NoteMatiereEnum.EA);
        bmEcriture.setRemarque(null);

        bulletin.setBulletinMatieres(List.of(bmCoran, bmEcriture));

        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));
        when(traductionService.findTraductionByCleAndValeur("cdmaticode", "CORAN"))
                .thenReturn(TraductionDto.builder().fr("Coran").build());
        when(traductionService.findTraductionByCleAndValeur("cdmaticode", "ECRITURE"))
                .thenReturn(TraductionDto.builder().fr("Écriture").build());

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lignes = (List<Map<String, String>>) variables.get("lignesMatieres");
        assertNotNull(lignes);
        assertEquals(2, lignes.size());

        Map<String, String> ligneCoran = lignes.get(0);
        assertEquals("Coran", ligneCoran.get("matiere"));
        assertEquals("A", ligneCoran.get("note"));
        assertEquals("Très bien", ligneCoran.get("remarque"));

        Map<String, String> ligneEcriture = lignes.get(1);
        assertEquals("Écriture", ligneEcriture.get("matiere"));
        assertEquals("EA", ligneEcriture.get("note"));
        assertEquals("", ligneEcriture.get("remarque"));
    }

    @Test
    public void testBuildTemplateVariablesLignesMatieresWhenBulletinMatieresIsNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setBulletinMatieres(null);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lignes = (List<Map<String, String>>) variables.get("lignesMatieres");
        assertNotNull(lignes);
        assertTrue(lignes.isEmpty());
    }

    @Test
    public void testBuildTemplateVariablesLignesMatieresIgnoreBulletinMatieresSansMatiereEntity() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        BulletinMatiereEntity bmSansMatiere = new BulletinMatiereEntity();
        bmSansMatiere.setMatiere(null);
        bulletin.setBulletinMatieres(List.of(bmSansMatiere));

        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lignes = (List<Map<String, String>>) variables.get("lignesMatieres");
        assertTrue(lignes.isEmpty());
    }

    @Test
    public void testBuildTemplateVariablesLignesMatieresUtiliseCodeBrutSiTraductionIntrouvable() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        MatiereEntity matiere = buildMatiere(MatiereEnum.FIQH);
        BulletinMatiereEntity bm = new BulletinMatiereEntity();
        bm.setMatiere(matiere);
        bm.setNote(NoteMatiereEnum.NA);
        bm.setRemarque(null);
        bulletin.setBulletinMatieres(List.of(bm));

        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));
        when(traductionService.findTraductionByCleAndValeur("cdmaticode", "FIQH"))
                .thenThrow(new ResourceNotFoundException("Traduction non trouvée"));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lignes = (List<Map<String, String>>) variables.get("lignesMatieres");
        assertEquals(1, lignes.size());
        assertEquals("FIQH", lignes.get(0).get("matiere"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — nbAbsences
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesNbAbsencesAvecValeur() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setNbAbsences(3);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals(3, variables.get("nbAbsences"));
    }

    @Test
    public void testBuildTemplateVariablesNbAbsencesDefaultZeroWhenNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setNbAbsences(null);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals(0, variables.get("nbAbsences"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — dateBulletin
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesDateBulletinAvecDate() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setDateBulletin(LocalDate.of(2025, 3, 15));
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("15/03/2025", variables.get("dateBulletin"));
    }

    @Test
    public void testBuildTemplateVariablesDateBulletinVideWhenNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setDateBulletin(null);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("dateBulletin"));
    }

    // -----------------------------------------------------------------------
    // buildTemplateVariables — appreciation
    // -----------------------------------------------------------------------

    @Test
    public void testBuildTemplateVariablesAppreciationAvecValeur() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setAppreciation("Élève sérieux et appliqué");
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("Élève sérieux et appliqué", variables.get("appreciation"));
    }

    @Test
    public void testBuildTemplateVariablesAppreciationVideWhenNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setAppreciation(null);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        Map<String, Object> variables = bulletinDocumentGenerator.buildTemplateVariables(bulletin);

        // THEN
        assertEquals("", variables.get("appreciation"));
    }

    // -----------------------------------------------------------------------
    // computeHash
    // -----------------------------------------------------------------------

    @Test
    public void testComputeHashRetourneValeurNonNulle() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setMois(3);
        bulletin.setAnnee(2025);
        bulletin.setNbAbsences(2);
        bulletin.setDateBulletin(LocalDate.of(2025, 3, 15));
        bulletin.setAppreciation("Bien");
        bulletin.setBulletinMatieres(new ArrayList<>());

        // WHEN
        String hash = bulletinDocumentGenerator.computeHash(bulletin);

        // THEN
        assertNotNull(hash);
        assertFalse(hash.isBlank());
    }

    @Test
    public void testComputeHashDifferentPourDeuxBulletinsDifferents() {
        // GIVEN
        BulletinEntity bulletin1 = buildBulletinMinimal(1L, 10L);
        bulletin1.setAppreciation("Très bien");
        bulletin1.setBulletinMatieres(new ArrayList<>());

        BulletinEntity bulletin2 = buildBulletinMinimal(2L, 10L);
        bulletin2.setAppreciation("Peut mieux faire");
        bulletin2.setBulletinMatieres(new ArrayList<>());

        // WHEN
        String hash1 = bulletinDocumentGenerator.computeHash(bulletin1);
        String hash2 = bulletinDocumentGenerator.computeHash(bulletin2);

        // THEN
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testComputeHashAvecBulletinMatieresNull() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(1L, 10L);
        bulletin.setBulletinMatieres(null);

        // WHEN — ne doit pas lever d'exception
        String hash = bulletinDocumentGenerator.computeHash(bulletin);

        // THEN
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // buildMetadata
    // -----------------------------------------------------------------------

    @Test
    public void testBuildMetadataContientIdBulletinNomPrenom() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(7L, 10L);
        EleveEntity eleve = buildEleve(10L, "Dupont", "Marie", null);
        when(eleveRepository.findById(10L)).thenReturn(Optional.of(eleve));

        // WHEN
        var metadata = bulletinDocumentGenerator.buildMetadata(bulletin);

        // THEN
        assertNotNull(metadata);
        assertEquals(3, metadata.size());
        assertTrue(metadata.stream().anyMatch(m -> "7".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "Dupont".equals(m.getValeur())));
        assertTrue(metadata.stream().anyMatch(m -> "Marie".equals(m.getValeur())));
    }

    @Test
    public void testBuildMetadataContientSeulementIdBulletinSiEleveIntrouvable() {
        // GIVEN
        BulletinEntity bulletin = buildBulletinMinimal(7L, 99L);
        when(eleveRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN
        var metadata = bulletinDocumentGenerator.buildMetadata(bulletin);

        // THEN
        assertNotNull(metadata);
        assertEquals(1, metadata.size());
        assertEquals("7", metadata.get(0).getValeur());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private BulletinEntity buildBulletinMinimal(Long id, Long idEleve) {
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setId(id);
        bulletin.setIdEleve(idEleve);
        bulletin.setBulletinMatieres(new ArrayList<>());
        return bulletin;
    }

    private EleveEntity buildEleve(Long id, String nom, String prenom, Long classeId) {
        EleveEntity eleve = new EleveEntity();
        eleve.setId(id);
        eleve.setNom(nom);
        eleve.setPrenom(prenom);
        // classeId est un champ @Formula, on le définit via réflexion
        try {
            java.lang.reflect.Field field = EleveEntity.class.getDeclaredField("classeId");
            field.setAccessible(true);
            field.set(eleve, classeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return eleve;
    }

    private UtilisateurEntity buildUtilisateur(String nom, String prenom) {
        UtilisateurEntity u = new UtilisateurEntity();
        u.setNom(nom);
        u.setPrenom(prenom);
        return u;
    }

    private MatiereEntity buildMatiere(MatiereEnum code) {
        MatiereEntity matiere = new MatiereEntity();
        matiere.setCode(code);
        return matiere;
    }
}
