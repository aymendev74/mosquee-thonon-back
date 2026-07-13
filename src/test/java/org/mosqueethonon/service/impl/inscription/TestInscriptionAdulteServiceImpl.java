package org.mosqueethonon.service.impl.inscription;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.repository.DocumentRepository;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionMatiereEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.mosqueethonon.service.impl.UserAccountManager;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteResultDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionAdulteDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.inscription.InscriptionAdulteMapper;
import org.mosqueethonon.v1.mapper.inscription.InscriptionAdulteMapperImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionAdulteServiceImpl {

    @Mock
    private InscriptionAdulteRepository inscriptionAdulteRepository;

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Spy
    private InscriptionAdulteMapper inscriptionAdulteMapper = new InscriptionAdulteMapperImpl();

    @Mock
    private TarifCalculService tarifCalculService;

    @Mock
    private ParamService paramService;

    @Mock
    private MailRequestRepository mailRequestRepository;

    @Mock
    private MatiereService matiereService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private TarifRepository tarifRepository;

    @Mock
    private UserAccountManager userService;

    @Mock
    private AsyncDocumentService asyncDocumentService;

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private InscriptionAdulteServiceImpl inscriptionAdulteService;

    private InscriptionAdulteDto inscriptionDto;
    private InscriptionAdulteEntity inscriptionEntity;
    private InscriptionSaveCriteria criteria;

    @BeforeEach
    public void setUp() {
        inscriptionDto = new InscriptionAdulteDto();
        inscriptionDto.setNom("Test Nom");
        inscriptionDto.setPrenom("Test Prenom");
        inscriptionDto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        inscriptionDto.setMatieres(Lists.newArrayList(MatiereEnum.TAFFSIR_CORAN));

        inscriptionEntity = new InscriptionAdulteEntity();
        inscriptionEntity.setId(1L);
        inscriptionEntity.setDateInscription(LocalDateTime.now());
        inscriptionEntity.setStatut(StatutInscription.PROVISOIRE);
        inscriptionEntity.setResponsableLegal(new ResponsableLegalEntity());
        InscriptionMatiereEntity inscriptionMatiere = new InscriptionMatiereEntity();
        MatiereEntity matiere = new MatiereEntity();
        matiere.setCode(MatiereEnum.TAFFSIR_CORAN);
        inscriptionMatiere.setMatiere(matiere);
        inscriptionEntity.setMatieres(Lists.newArrayList(inscriptionMatiere));

        criteria = InscriptionSaveCriteria.builder().sendMailConfirmation(true).build();
        ReflectionTestUtils.setField(inscriptionAdulteService, "userAccountManager", userService);
        ReflectionTestUtils.setField(inscriptionAdulteService, "inscriptionRepository", inscriptionRepository);
        ReflectionTestUtils.setField(inscriptionAdulteService, "mailRequestRepository", mailRequestRepository);

        // Stub par défaut : aucun document trouvé (évite NPE dans les tests existants)
        lenient().when(documentRepository.findByMetadataKeyAndValue(any(), anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void testCreateInscription_Success() {
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(true);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class), eq(inscriptionDto.getStatutProfessionnel()))).thenReturn(tarifDto);
        when(this.matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(this.userService.findByEmail(any())).thenReturn(Optional.empty());
        when(this.userService.createUser(any())).thenAnswer(invocation -> {
            UserDto user = invocation.getArgument(0);
            user.setId(99L);
            return user;
        });

        InscriptionAdulteResultDto result = inscriptionAdulteService.createInscription(inscriptionDto);

        assertNotNull(result);
        assertTrue(result.getNewlyCreatedAccount());
        assertFalse(result.getEnabledAccount());
        verify(inscriptionAdulteRepository, times(1)).save(any());
        verify(mailRequestRepository, times(1)).save(any());
    }

    @Test
    public void testCreateInscription_InscriptionClosed() {
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> inscriptionAdulteService.createInscription(new InscriptionAdulteDto()));
    }

    @Test
    public void testFindInscriptionById_NotFound() {
        // Arrange
        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.findInscriptionById(1L);

        // Assert
        assertNull(result);
        verify(inscriptionAdulteRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateInscription_Success() {
        // Arrange
        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.of(inscriptionEntity));
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(anyLong(), any(LocalDate.class), eq(inscriptionDto.getStatutProfessionnel()))).thenReturn(tarifDto);
        when(this.matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.updateInscription(1L, inscriptionDto, criteria);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getMatieres().size());
        verify(inscriptionAdulteRepository, times(1)).findById(1L);
        verify(inscriptionAdulteRepository, times(1)).save(any());
        verify(mailRequestRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateInscription_NotFound() {
        // Arrange
        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            inscriptionAdulteService.updateInscription(1L, inscriptionDto, criteria);
        });

        assertEquals("L'inscription adulte n'a pas été trouvée ! id = 1", exception.getMessage());
        verify(inscriptionAdulteRepository, times(1)).findById(1L);
        verify(inscriptionAdulteRepository, times(0)).save(any(InscriptionAdulteEntity.class));
    }

    @Test
    public void testFindNbInscriptionsByPeriode() {
        // Arrange
        when(inscriptionRepository.getNbElevesInscritsByIdPeriode(anyLong(), anyString())).thenReturn(10);

        // Act
        Integer nbInscriptions = inscriptionAdulteService.findNbInscriptionsByPeriode(1L);

        // Assert
        assertEquals(10, nbInscriptions);
        verify(inscriptionRepository, times(1)).getNbElevesInscritsByIdPeriode(anyLong(), anyString());
    }

    @Test
    public void testIsInscriptionOutsidePeriode() {
        // Arrange
        PeriodeDto periode = new PeriodeDto();
        periode.setDateDebut(LocalDate.now().minusDays(1));
        periode.setDateFin(LocalDate.now().plusDays(1));

        when(inscriptionRepository.getNbInscriptionOutsideRange(anyLong(), any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(1);

        // Act
        boolean result = inscriptionAdulteService.isInscriptionOutsidePeriode(1L, periode);

        // Assert
        assertTrue(result);
        verify(inscriptionRepository, times(1)).getNbInscriptionOutsideRange(anyLong(), any(LocalDate.class), any(LocalDate.class), anyString());
    }

    @Test
    public void testFindInscriptionsByUtilisateurConnecte_Success() {
        // Arrange
        String username = "testuser";
        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setId(1L);

        ResponsableLegalEntity responsableLegal = new ResponsableLegalEntity();
        responsableLegal.setNom("Dupont");
        responsableLegal.setPrenom("Jean");
        responsableLegal.setEmail("jean.dupont@test.com");
        responsableLegal.setMobile("0612345678");

        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        periode.setAnneeDebut(2024);
        periode.setAnneeFin(2025);

        TarifEntity tarif = new TarifEntity();
        tarif.setId(1L);
        tarif.setPeriode(periode);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(1L);
        eleve.setDateNaissance(LocalDate.of(1990, 5, 15));
        eleve.setSexe(SexeEnum.M);

        MatiereEntity matiere = new MatiereEntity();
        matiere.setCode(MatiereEnum.TAFFSIR_CORAN);

        InscriptionMatiereEntity inscriptionMatiere = new InscriptionMatiereEntity();
        inscriptionMatiere.setMatiere(matiere);

        InscriptionAdulteEntity inscription = new InscriptionAdulteEntity();
        inscription.setId(1L);
        inscription.setIdTarif(1L);
        inscription.setStatut(StatutInscription.VALIDEE);
        inscription.setMontantTotal(BigDecimal.valueOf(200));
        inscription.setNoInscription("AMC-002");
        inscription.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        inscription.setResponsableLegal(responsableLegal);
        inscription.setEleves(List.of(eleve));
        inscription.setMatieres(List.of(inscriptionMatiere));

        when(securityContext.getUser()).thenReturn(username);
        when(utilisateurRepository.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(inscriptionAdulteRepository.findByUtilisateurIdWithEleves(1L)).thenReturn(List.of(inscription));
        when(inscriptionAdulteRepository.fetchMatieres(List.of(inscription))).thenReturn(List.of(inscription));
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));

        // Act
        List<InscriptionAdulteParAnneeScolaireDto> result = inscriptionAdulteService.findInscriptionsByUtilisateurConnecte();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getAnneeDebut());
        assertEquals(2025, result.get(0).getAnneeFin());
        assertEquals(StatutInscription.VALIDEE, result.get(0).getStatut());
        assertEquals(BigDecimal.valueOf(200), result.get(0).getMontantTotal());
        assertEquals("AMC-002", result.get(0).getNoInscription());
        assertEquals("Dupont", result.get(0).getNom());
        assertEquals("Jean", result.get(0).getPrenom());
        assertEquals("jean.dupont@test.com", result.get(0).getEmail());
        assertEquals(StatutProfessionnelEnum.AVEC_ACTIVITE, result.get(0).getStatutProfessionnel());
        assertEquals(1, result.get(0).getMatieres().size());
        assertEquals(MatiereEnum.TAFFSIR_CORAN, result.get(0).getMatieres().get(0));
    }

    @Test
    public void testFindInscriptionsByUtilisateurConnecte_NoUserConnected() {
        // Arrange
        when(securityContext.getUser()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> inscriptionAdulteService.findInscriptionsByUtilisateurConnecte());
    }

    @Test
    public void testFindInscriptionsByUtilisateurConnecte_UserNotFound() {
        // Arrange
        when(securityContext.getUser()).thenReturn("unknownuser");
        when(utilisateurRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> inscriptionAdulteService.findInscriptionsByUtilisateurConnecte());
    }

    @Test
    public void testReinscription_Success() {
        // Arrange
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(true);
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(2L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);
        when(this.matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(this.userService.findByEmail(any())).thenReturn(Optional.empty());
        when(this.userService.createUser(any())).thenAnswer(invocation -> {
            UserDto user = invocation.getArgument(0);
            user.setId(99L);
            return user;
        });

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class), eq(StatutProfessionnelEnum.AVEC_ACTIVITE))).thenReturn(tarifDto);

        ReinscriptionAdulteDto reinscriptionDto = new ReinscriptionAdulteDto();
        reinscriptionDto.setNom("Dupont");
        reinscriptionDto.setPrenom("Jean");
        reinscriptionDto.setEmail("jean.dupont@test.com");
        reinscriptionDto.setMobile("0612345678");
        reinscriptionDto.setNumeroEtRue("10 rue de la paix");
        reinscriptionDto.setCodePostal(74200);
        reinscriptionDto.setVille("Thonon");
        reinscriptionDto.setDateNaissance(LocalDate.of(1990, 5, 15));
        reinscriptionDto.setSexe(SexeEnum.M);
        reinscriptionDto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        reinscriptionDto.setMatieres(Lists.newArrayList(MatiereEnum.TAFFSIR_CORAN));

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.reinscription(reinscriptionDto);

        // Assert
        assertNotNull(result);
        verify(inscriptionAdulteRepository, times(1)).save(any(InscriptionAdulteEntity.class));
        verify(mailRequestRepository, times(1)).save(any());
    }

    @Test
    public void testReinscription_PositionneFlagReinscriptionTrue() {
        // Arrange
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(true);
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(2L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);
        when(this.matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(this.userService.findByEmail(any())).thenReturn(Optional.empty());
        when(this.userService.createUser(any())).thenAnswer(invocation -> {
            UserDto user = invocation.getArgument(0);
            user.setId(99L);
            return user;
        });

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class), eq(StatutProfessionnelEnum.AVEC_ACTIVITE))).thenReturn(tarifDto);

        ReinscriptionAdulteDto reinscriptionDto = new ReinscriptionAdulteDto();
        reinscriptionDto.setNom("Dupont");
        reinscriptionDto.setPrenom("Jean");
        reinscriptionDto.setEmail("jean.dupont@test.com");
        reinscriptionDto.setMobile("0612345678");
        reinscriptionDto.setNumeroEtRue("10 rue de la paix");
        reinscriptionDto.setCodePostal(74200);
        reinscriptionDto.setVille("Thonon");
        reinscriptionDto.setDateNaissance(LocalDate.of(1990, 5, 15));
        reinscriptionDto.setSexe(SexeEnum.M);
        reinscriptionDto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        reinscriptionDto.setMatieres(Lists.newArrayList(MatiereEnum.TAFFSIR_CORAN));

        // Act
        inscriptionAdulteService.reinscription(reinscriptionDto);

        // Assert — l'entité persistée porte le flag réinscription à true
        ArgumentCaptor<InscriptionAdulteEntity> captor = ArgumentCaptor.forClass(InscriptionAdulteEntity.class);
        verify(inscriptionAdulteRepository).save(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getReinscription());
    }

    @Test
    public void testCreateInscription_FlagReinscriptionFalse() {
        // Une inscription normale doit persister reinscription=false, pas null.
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(true);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder().idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class), eq(inscriptionDto.getStatutProfessionnel()))).thenReturn(tarifDto);
        when(this.matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(this.userService.findByEmail(any())).thenReturn(Optional.empty());
        when(this.userService.createUser(any())).thenAnswer(invocation -> {
            UserDto user = invocation.getArgument(0);
            user.setId(99L);
            return user;
        });

        // Act
        inscriptionAdulteService.createInscription(inscriptionDto);

        // Assert
        ArgumentCaptor<InscriptionAdulteEntity> captor = ArgumentCaptor.forClass(InscriptionAdulteEntity.class);
        verify(inscriptionAdulteRepository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getReinscription());
    }

    @Test
    public void testReinscription_InscriptionClosed() {
        // Arrange
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(false);

        ReinscriptionAdulteDto reinscriptionDto = new ReinscriptionAdulteDto();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> inscriptionAdulteService.reinscription(reinscriptionDto));
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument peuplé
    // ---------------------------------------------------------------------------

    @Test
    public void testFindInscriptionById_IdDocumentPeuple_QuandDocumentTrouve() {
        // Arrange
        Long id = 1L;
        DocumentEntity doc = new DocumentEntity();
        doc.setId(77L);

        when(inscriptionAdulteRepository.findById(id)).thenReturn(Optional.of(inscriptionEntity));
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_INSCRIPTION), eq(String.valueOf(id))))
                .thenReturn(Optional.of(doc));

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.findInscriptionById(id);

        // Assert
        assertNotNull(result);
        assertEquals(77L, result.getIdDocument());
    }

    @Test
    public void testFindInscriptionById_IdDocumentNull_QuandAucunDocument() {
        // Arrange
        Long id = 1L;
        when(inscriptionAdulteRepository.findById(id)).thenReturn(Optional.of(inscriptionEntity));
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_INSCRIPTION), eq(String.valueOf(id))))
                .thenReturn(Optional.empty());

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.findInscriptionById(id);

        // Assert
        assertNotNull(result);
        assertNull(result.getIdDocument());
    }

    @Test
    public void testUpdateInscription_IdDocumentPeuple_QuandDocumentTrouve() {
        // Arrange
        DocumentEntity doc = new DocumentEntity();
        doc.setId(88L);

        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.of(inscriptionEntity));
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);
        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder()
                .idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(anyLong(), any(LocalDate.class),
                eq(inscriptionDto.getStatutProfessionnel()))).thenReturn(tarifDto);
        when(matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKey.ID_INSCRIPTION), eq("1")))
                .thenReturn(Optional.of(doc));

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.updateInscription(1L, inscriptionDto,
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        // Assert
        assertNotNull(result);
        assertEquals(88L, result.getIdDocument());
    }

    // ---------------------------------------------------------------------------
    // Tests conditionnement de la génération de document selon le statut
    // ---------------------------------------------------------------------------

    @Test
    public void testCreateInscription_DocumentGenere_QuandStatutProvisoire() {
        // GIVEN — createInscription fixe toujours PROVISOIRE ; l'entité retournée par save
        // a également PROVISOIRE (défini dans setUp), donc la condition est vraie.
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(true);
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder()
                .idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class),
                eq(inscriptionDto.getStatutProfessionnel()))).thenReturn(tarifDto);
        when(matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(userService.findByEmail(any())).thenReturn(Optional.empty());
        when(userService.createUser(any())).thenAnswer(invocation -> {
            UserDto user = invocation.getArgument(0);
            user.setId(99L);
            return user;
        });

        // WHEN
        InscriptionAdulteResultDto result = inscriptionAdulteService.createInscription(inscriptionDto);

        // THEN
        assertNotNull(result);
        verify(asyncDocumentService, times(1))
                .requestDocumentGeneration(eq(DocumentRequestType.INSCRIPTION_ADULTE), anyLong());
        verify(mailRequestRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateInscription_DocumentNonGenere_QuandStatutRefuse() {
        // GIVEN — save retourne une entité avec statut REFUSE : la condition PROVISOIRE || VALIDEE
        // est fausse, donc requestDocumentGeneration ne doit pas être appelé.
        InscriptionAdulteEntity entityRefuse = new InscriptionAdulteEntity();
        entityRefuse.setId(1L);
        entityRefuse.setDateInscription(java.time.LocalDateTime.now());
        entityRefuse.setStatut(StatutInscription.REFUSE);
        entityRefuse.setResponsableLegal(new ResponsableLegalEntity());
        InscriptionMatiereEntity inscriptionMatiereRefuse = new InscriptionMatiereEntity();
        MatiereEntity matiereRefuse = new MatiereEntity();
        matiereRefuse.setCode(MatiereEnum.TAFFSIR_CORAN);
        inscriptionMatiereRefuse.setMatiere(matiereRefuse);
        entityRefuse.setMatieres(Lists.newArrayList(inscriptionMatiereRefuse));

        when(inscriptionAdulteRepository.findById(1L)).thenReturn(Optional.of(inscriptionEntity));
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(entityRefuse);

        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder()
                .idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(anyLong(), any(LocalDate.class),
                eq(inscriptionDto.getStatutProfessionnel()))).thenReturn(tarifDto);
        when(matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));

        // WHEN
        InscriptionAdulteDto result = inscriptionAdulteService.updateInscription(1L, inscriptionDto,
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        // THEN
        assertNotNull(result);
        verify(asyncDocumentService, never())
                .requestDocumentGeneration(any(), anyLong());
        verify(mailRequestRepository, never()).save(any());
    }

    @Test
    public void testReinscription_IdDocumentNonPeuple_CarAsynchrone() {
        // La réinscription appelle requestDocumentGeneration de façon async,
        // le document n'est pas encore disponible au retour de la méthode.
        // Arrange
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(true);
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(2L);
        when(inscriptionAdulteRepository.save(any(InscriptionAdulteEntity.class))).thenReturn(inscriptionEntity);
        when(matiereService.findByCode(MatiereEnum.TAFFSIR_CORAN)).thenReturn(Optional.of(new MatiereEntity()));
        when(userService.findByEmail(any())).thenReturn(Optional.empty());
        when(userService.createUser(any())).thenAnswer(invocation -> {
            UserDto user = invocation.getArgument(0);
            user.setId(99L);
            return user;
        });
        TarifInscriptionAdulteDto tarifDto = TarifInscriptionAdulteDto.builder()
                .idTari(123L).tarif(new BigDecimal("100.0")).build();
        when(tarifCalculService.calculTarifInscriptionAdulte(isNull(), any(LocalDate.class),
                eq(StatutProfessionnelEnum.AVEC_ACTIVITE))).thenReturn(tarifDto);

        ReinscriptionAdulteDto reinscriptionDto = new ReinscriptionAdulteDto();
        reinscriptionDto.setNom("Dupont");
        reinscriptionDto.setPrenom("Jean");
        reinscriptionDto.setEmail("jean.dupont@test.com");
        reinscriptionDto.setMobile("0612345678");
        reinscriptionDto.setNumeroEtRue("10 rue de la paix");
        reinscriptionDto.setCodePostal(74200);
        reinscriptionDto.setVille("Thonon");
        reinscriptionDto.setDateNaissance(LocalDate.of(1990, 5, 15));
        reinscriptionDto.setSexe(org.mosqueethonon.enums.SexeEnum.M);
        reinscriptionDto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        reinscriptionDto.setMatieres(new ArrayList<>(List.of(MatiereEnum.TAFFSIR_CORAN)));

        // Act
        InscriptionAdulteDto result = inscriptionAdulteService.reinscription(reinscriptionDto);

        // Assert — le DTO retourné ne contient pas d'idDocument (généré en asynchrone)
        assertNotNull(result);
        assertNull(result.getIdDocument());
        verify(asyncDocumentService, times(1))
                .requestDocumentGeneration(eq(org.mosqueethonon.enums.DocumentRequestType.INSCRIPTION_ADULTE), anyLong());
    }
}
