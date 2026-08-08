package org.mosqueethonon.inscription.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.config.TimeConfiguration;
import org.mosqueethonon.common.security.context.SecurityContext;
import org.mosqueethonon.document.entity.DocumentEntity;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
import org.mosqueethonon.inscription.entity.ResponsableLegalEntity;
import org.mosqueethonon.referentiel.entity.PeriodeEntity;
import org.mosqueethonon.tarif.entity.TarifEntity;
import org.mosqueethonon.document.enums.DocumentMetadataKeyEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;
import org.mosqueethonon.inscription.enums.NiveauScolaireEnum;
import org.mosqueethonon.inscription.enums.ResultatEnum;
import org.mosqueethonon.inscription.enums.TypeInscriptionEnum;
import org.mosqueethonon.inscription.service.Incoherences;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;
import org.mosqueethonon.referentiel.repository.NiveauRepository;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.document.repository.DocumentRepository;
import org.mosqueethonon.inscription.repository.EleveRepository;
import org.mosqueethonon.inscription.repository.InscriptionEnfantRepository;
import org.mosqueethonon.inscription.repository.InscriptionRepository;
import org.mosqueethonon.mail.repository.MailRequestRepository;
import org.mosqueethonon.referentiel.repository.PeriodeRepository;
import org.mosqueethonon.tarif.repository.TarifRepository;
import org.mosqueethonon.document.service.AsyncDocumentService;
import org.mosqueethonon.utilisateur.service.impl.UserAccountManager;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.mosqueethonon.paiement.service.PaiementService;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;
import org.mosqueethonon.param.service.ParamService;
import org.mosqueethonon.tarif.service.TarifCalculService;
import org.mosqueethonon.inscription.v1.dto.EleveDto;
import org.mosqueethonon.inscription.v1.dto.EleveReinscriptionDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantResultDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.inscription.v1.dto.ReinscriptionDto;
import org.mosqueethonon.inscription.v1.dto.ResponsableLegalDto;
import org.mosqueethonon.tarif.v1.dto.TarifInscriptionEnfantDto;
import org.mosqueethonon.utilisateur.v1.dto.UserDto;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;
import org.mosqueethonon.inscription.v1.mapper.EleveMapper;
import org.mosqueethonon.inscription.v1.mapper.InscriptionEnfantMapper;
import org.mosqueethonon.inscription.v1.mapper.InscriptionEnfantMapperImpl;
import org.mosqueethonon.inscription.v1.mapper.ResponsableLegalMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionEnfantServiceImpl {

    @Mock
    private InscriptionEnfantRepository inscriptionEnfantRepository;
    @Mock
    private InscriptionRepository inscriptionRepository;
    @Mock
    private ResponsableLegalMapper responsableLegalMapper;
    @Mock
    private EleveMapper eleveMapper;
    @Spy
    private InscriptionEnfantMapper inscriptionEnfantMapper = new InscriptionEnfantMapperImpl(eleveMapper, responsableLegalMapper);
    @Mock
    private TarifCalculService tarifCalculService;
    @Mock
    private MailRequestRepository mailRequestRepository;
    @Mock
    private ParamService paramService;
    @Mock
    private TarifRepository tarifRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private EleveRepository eleveRepository;
    @Mock
    private NiveauRepository niveauRepository;
    @Mock
    private UserAccountManager userService;
    @Mock
    private PeriodeRepository periodeRepository;
    @Mock
    private AsyncDocumentService asyncDocumentService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private PaiementService paiementService;
    /**
     * Horloge figée sur le fuseau de l'application, injectée dans le service par {@code @InjectMocks}.
     * Fixtures et service doivent lire la même horloge, sinon le fuseau de la machine qui exécute les
     * tests décale la comparaison d'un jour.
     */
    private static final Clock HORLOGE_FIGEE = Clock.fixed(
            LocalDate.of(2026, Month.MARCH, 15).atStartOfDay(TimeConfiguration.ZONE_APPLICATION).toInstant(),
            TimeConfiguration.ZONE_APPLICATION);

    /** Ce que le service obtient lorsqu'il appelle {@code LocalDate.now(clock)}. */
    private static final LocalDate AUJOURD_HUI = LocalDate.now(HORLOGE_FIGEE);

    @Spy
    private Clock clock = HORLOGE_FIGEE;

    @InjectMocks
    private InscriptionEnfantServiceImpl underTest;

    @BeforeEach
    public void injectParentFields() {
        ReflectionTestUtils.setField(underTest, "userAccountManager", userService);
        ReflectionTestUtils.setField(underTest, "inscriptionRepository", inscriptionRepository);
        ReflectionTestUtils.setField(underTest, "mailRequestRepository", mailRequestRepository);
        ReflectionTestUtils.setField(underTest, "periodeRepository", periodeRepository);

        // Les @Mock eleveMapper et responsableLegalMapper peuvent être null dans le @Spy
        // au moment de sa construction inline. On les injecte via le setter et la réflexion.
        inscriptionEnfantMapper.setEleveMapper(eleveMapper);
        ReflectionTestUtils.setField(inscriptionEnfantMapper, "responsableLegalMapper", responsableLegalMapper);

        // Mock par défaut pour lockPeriodeActive
        PeriodeEntity periodeMock = new PeriodeEntity();
        periodeMock.setId(99L);
        lenient().when(periodeRepository.findByApplicationAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(any(), any(), any())).thenReturn(Optional.of(periodeMock));
        lenient().when(periodeRepository.lockById(99L)).thenReturn(Optional.of(periodeMock));

        // Stub par défaut : aucun document trouvé (évite NPE dans les tests existants)
        lenient().when(documentRepository.findByMetadataKeyAndValue(any(), anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void testSaveInscriptionExpectIllegalStateExceptionWhenInscriptionDisabled() {
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.FALSE);
        assertThrows(IllegalStateException.class,
                () -> {
                    this.underTest.createInscription(null);
                });
    }

    @Test
    public void testSaveInscriptionExpectIllegalStateExceptionWhenReinscriptionPrioritaireEnabled() {
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.TRUE);
        assertThrows(IllegalStateException.class,
                () -> {
                    this.underTest.createInscription(null);
                });
    }

    @Test
    public void testCreateInscription_WithNewUserAccount() {
        // GIVEN
        final Long numeroInscription = Long.valueOf(1001);
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(2);
        inscriptionEnfantDto.getResponsableLegal().setEmail("test@example.com");
        inscriptionEnfantDto.getResponsableLegal().setNom("Dupont");
        inscriptionEnfantDto.getResponsableLegal().setPrenom("Jean");
        
        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(2);
        
        UserDto createdUserDto = new UserDto();
        createdUserDto.setId(1L);
        
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(this.userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(numeroInscription);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);

        // WHEN
        InscriptionEnfantResultDto result = this.underTest.createInscription(inscriptionEnfantDto);

        // THEN
        assertNotNull(result);
        assertTrue(result.getNewlyCreatedAccount());
        assertFalse(result.getEnabledAccount());
        assertEquals(StatutInscriptionEnum.PROVISOIRE, result.getStatut());
        verify(this.userService).createUser(any(UserDto.class));
        verify(this.mailRequestRepository).save(any());
        verify(this.inscriptionEnfantRepository).save(any());
    }

    @Test
    public void testCreateInscription_WithExistingActiveUserAccount() {
        // GIVEN
        final Long numeroInscription = Long.valueOf(1001);
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(2);
        inscriptionEnfantDto.getResponsableLegal().setEmail("existing@example.com");
        
        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(2);
        UserDto existingUser = new UserDto();
        existingUser.setId(2L);
        existingUser.setEnabled(true);
        
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.userService.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(numeroInscription);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);

        // WHEN
        InscriptionEnfantResultDto result = this.underTest.createInscription(inscriptionEnfantDto);

        // THEN
        assertNotNull(result);
        assertFalse(result.getNewlyCreatedAccount());
        assertTrue(result.getEnabledAccount());
        assertEquals(StatutInscriptionEnum.PROVISOIRE, result.getStatut());
        verify(this.userService, never()).createUser(any());
        verify(this.mailRequestRepository).save(any());
        verify(this.inscriptionEnfantRepository).save(any());
    }

    @Test
    public void testCreateInscription_WithExistingInactiveUserAccount() {
        // GIVEN
        final Long numeroInscription = Long.valueOf(1001);
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(2);
        inscriptionEnfantDto.getResponsableLegal().setEmail("inactive@example.com");
        
        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(2);
        UserDto existingUser = new UserDto();
        existingUser.setId(3L);
        existingUser.setEnabled(false);
        
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.userService.findByEmail("inactive@example.com")).thenReturn(Optional.of(existingUser));
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(numeroInscription);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);

        // WHEN
        InscriptionEnfantResultDto result = this.underTest.createInscription(inscriptionEnfantDto);

        // THEN
        assertNotNull(result);
        assertFalse(result.getNewlyCreatedAccount());
        assertFalse(result.getEnabledAccount());
        assertEquals(StatutInscriptionEnum.PROVISOIRE, result.getStatut());
        verify(this.userService, never()).createUser(any());
        verify(this.mailRequestRepository).save(any());
        verify(this.inscriptionEnfantRepository).save(any());
    }

    private InscriptionEnfantEntity createInscriptionEntity(Integer nbEleves) {
        InscriptionEnfantEntity inscriptionEnfantEntity = new InscriptionEnfantEntity();
        inscriptionEnfantEntity.setResponsableLegal(new ResponsableLegalEntity());
        inscriptionEnfantEntity.setEleves(new ArrayList<>());
        for(int i = 0; i < nbEleves ; i++) {
            inscriptionEnfantEntity.getEleves().add(new EleveEntity());
        }
        return inscriptionEnfantEntity;
    }

    private InscriptionEnfantEntity createInscriptionEntityWithDate(Integer nbEleves) {
        InscriptionEnfantEntity entity = createInscriptionEntity(nbEleves);
        entity.setDateInscription(LocalDateTime.now(clock));
        return entity;
    }

    private TarifInscriptionEnfantDto createTarifInscription() {
        return TarifInscriptionEnfantDto.builder().idTariEleve(1L).idTariBase(2L)
                .tarifEleve(BigDecimal.valueOf(12)).tarifBase(BigDecimal.valueOf(165))
                .listeAttente(Boolean.FALSE).build();
    }

    /** Inscription dont le responsable légal a un email : prérequis de {@code normalize()}. */
    private InscriptionEnfantDto createInscriptionNormalisable(int nbEleves) {
        InscriptionEnfantDto dto = createInscription(nbEleves);
        dto.getResponsableLegal().setEmail("test@example.com");
        return dto;
    }

    private InscriptionEnfantDto createInscription(int nbEleves) {
        InscriptionEnfantDto inscriptionEnfantDto = new InscriptionEnfantDto();
        inscriptionEnfantDto.setResponsableLegal(ResponsableLegalDto.builder().build());
        inscriptionEnfantDto.setEleves(new ArrayList<>());
        for(int i = 0; i < nbEleves ; i++) {
            inscriptionEnfantDto.getEleves().add(EleveDto.builder().build());
        }
        return inscriptionEnfantDto;
    }

    @Test
    public void testUpdateInscriptionExpectResourceNotFoundExceptionWhenInscriptionDoesNotExist() {
        InscriptionEnfantDto inscriptionEnfantDto = new InscriptionEnfantDto();
        assertThrows(ResourceNotFoundException.class,
                () -> {
                    this.underTest.updateInscription(null, inscriptionEnfantDto, InscriptionSaveCriteria.builder().build());
                });
    }

    @Test
    public void testFindInscriptionsByUtilisateurConnecte_Success() {
        // Arrange
        String username = "testuser";
        UserDto utilisateur = new UserDto();
        utilisateur.setId(1L);
        utilisateur.setUsername(username);

        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        periode.setAnneeDebut(2024);
        periode.setAnneeFin(2025);

        TarifEntity tarif = new TarifEntity();
        tarif.setId(1L);
        tarif.setPeriode(periode);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(1L);
        eleve.setNom("Dupont");
        eleve.setPrenom("Marie");

        ResponsableLegalEntity responsableLegal = new ResponsableLegalEntity();
        responsableLegal.setId(1L);
        responsableLegal.setNom("Dupont");
        responsableLegal.setAutorisationAutonomie(true);
        responsableLegal.setAutorisationMedia(false);

        ResponsableLegalDto responsableLegalDto = ResponsableLegalDto.builder()
                .nom("Dupont")
                .autorisationAutonomie(true)
                .autorisationMedia(false)
                .build();

        InscriptionEnfantEntity inscription = new InscriptionEnfantEntity();
        inscription.setId(1L);
        inscription.setIdTarif(1L);
        inscription.setStatut(StatutInscriptionEnum.VALIDEE);
        inscription.setMontantTotal(BigDecimal.valueOf(150));
        inscription.setNoInscription("AMC-001");
        inscription.setResponsableLegal(responsableLegal);
        inscription.setEleves(List.of(eleve));

        EleveDto eleveDto = EleveDto.builder()
                .id(1L)
                .nom("Dupont")
                .prenom("Marie")
                .build();

        when(securityContext.getUser()).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(inscriptionEnfantRepository.findByUtilisateurId(1L)).thenReturn(List.of(inscription));
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));
        when(eleveMapper.fromEntityToDto(eleve)).thenReturn(eleveDto);
        when(responsableLegalMapper.fromEntityToDto(responsableLegal)).thenReturn(responsableLegalDto);

        // Act
        List<InscriptionEnfantParAnneeScolaireDto> result = underTest.findInscriptionsByUtilisateurConnecte();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getAnneeDebut());
        assertEquals(2025, result.get(0).getAnneeFin());
        assertEquals(StatutInscriptionEnum.VALIDEE, result.get(0).getStatut());
        assertEquals(BigDecimal.valueOf(150), result.get(0).getMontantTotal());
        assertEquals("AMC-001", result.get(0).getNoInscription());
        assertNotNull(result.get(0).getResponsableLegal());
        assertEquals("Dupont", result.get(0).getResponsableLegal().getNom());
        assertEquals(1, result.get(0).getEleves().size());
        assertEquals("Dupont", result.get(0).getEleves().get(0).getNom());
        assertEquals("Marie", result.get(0).getEleves().get(0).getPrenom());
    }

    @Test
    public void testFindInscriptionsByUtilisateurConnecte_NoUserConnected() {
        // Arrange
        when(securityContext.getUser()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> underTest.findInscriptionsByUtilisateurConnecte());
    }

    @Test
    public void testFindInscriptionsByUtilisateurConnecte_UserNotFound() {
        // Arrange
        when(securityContext.getUser()).thenReturn("unknownuser");
        when(userService.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> underTest.findInscriptionsByUtilisateurConnecte());
    }

    @Test
    public void testReinscription_Success() {
        // Arrange
        String username = "testuser";
        UserDto utilisateur = new UserDto();
        utilisateur.setId(1L);

        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        periode.setIdPeriodePrecedente(0L);

        TarifEntity tarif = new TarifEntity();
        tarif.setId(1L);
        tarif.setPeriode(periode);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(1L);
        eleve.setNom("Dupont");
        eleve.setPrenom("Marie");
        eleve.setIdInscription(1L);
        eleve.setIdTarif(1L);

        ResponsableLegalEntity responsableLegalReinscription = new ResponsableLegalEntity();

        InscriptionEnfantEntity ancienneInscription = new InscriptionEnfantEntity();
        ancienneInscription.setId(1L);
        ancienneInscription.setIdUtilisateur(utilisateur.getId());

        EleveReinscriptionDto eleveReinscription = EleveReinscriptionDto.builder()
                .id(1L)
                .niveau(NiveauScolaireEnum.CP)
                .build();

        ReinscriptionDto reinscriptionDto = new ReinscriptionDto();
        reinscriptionDto.setEleves(List.of(eleveReinscription));
        reinscriptionDto.setResponsableLegal(ResponsableLegalDto.builder().build());

        InscriptionEnfantDto inscriptionDto = new InscriptionEnfantDto();

        when(paramService.isInscriptionEnfantEnabled()).thenReturn(true);
        when(paramService.isReinscriptionPrioritaireEnabled()).thenReturn(true);
        when(securityContext.getUser()).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(eleveRepository.findAllById(List.of(1L))).thenReturn(List.of(eleve));
        when(inscriptionEnfantRepository.findById(1L)).thenReturn(Optional.of(ancienneInscription));
        when(responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal())).thenReturn(responsableLegalReinscription);
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));
        when(inscriptionRepository.findFirstEleveByNomPrenomDateNaissanceIdPeriode(any(), any(), any(), any())).thenReturn(eleve);
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        InscriptionEnfantEntity savedInscription = new InscriptionEnfantEntity();
        savedInscription.setStatut(StatutInscriptionEnum.VALIDEE);
        when(inscriptionEnfantRepository.save(any())).thenReturn(savedInscription);
        when(inscriptionEnfantMapper.fromEntityToDto(any())).thenReturn(inscriptionDto);

        // Act
        InscriptionEnfantDto result = underTest.reinscription(reinscriptionDto);

        // Assert
        assertNotNull(result);
        verify(responsableLegalMapper).fromDtoToEntity(reinscriptionDto.getResponsableLegal());
        verify(inscriptionEnfantRepository).save(any());
        verify(mailRequestRepository).save(any());
    }

    @Test
    public void testReinscription_PositionneFlagReinscriptionTrue() {
        // Arrange (happy path de réinscription)
        String username = "testuser";
        UserDto utilisateur = new UserDto();
        utilisateur.setId(1L);

        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        periode.setIdPeriodePrecedente(0L);

        TarifEntity tarif = new TarifEntity();
        tarif.setId(1L);
        tarif.setPeriode(periode);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(1L);
        eleve.setNom("Dupont");
        eleve.setPrenom("Marie");
        eleve.setIdInscription(1L);
        eleve.setIdTarif(1L);

        InscriptionEnfantEntity ancienneInscription = new InscriptionEnfantEntity();
        ancienneInscription.setId(1L);
        ancienneInscription.setIdUtilisateur(utilisateur.getId());

        EleveReinscriptionDto eleveReinscription = EleveReinscriptionDto.builder()
                .id(1L).niveau(NiveauScolaireEnum.CP).build();

        ReinscriptionDto reinscriptionDto = new ReinscriptionDto();
        reinscriptionDto.setEleves(List.of(eleveReinscription));
        reinscriptionDto.setResponsableLegal(ResponsableLegalDto.builder().build());

        when(paramService.isInscriptionEnfantEnabled()).thenReturn(true);
        when(paramService.isReinscriptionPrioritaireEnabled()).thenReturn(true);
        when(securityContext.getUser()).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(eleveRepository.findAllById(List.of(1L))).thenReturn(List.of(eleve));
        when(inscriptionEnfantRepository.findById(1L)).thenReturn(Optional.of(ancienneInscription));
        when(responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal())).thenReturn(new ResponsableLegalEntity());
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));
        when(inscriptionRepository.findFirstEleveByNomPrenomDateNaissanceIdPeriode(any(), any(), any(), any())).thenReturn(eleve);
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        InscriptionEnfantEntity savedInscription = new InscriptionEnfantEntity();
        savedInscription.setStatut(StatutInscriptionEnum.VALIDEE);
        when(inscriptionEnfantRepository.save(any())).thenReturn(savedInscription);
        when(inscriptionEnfantMapper.fromEntityToDto(any())).thenReturn(new InscriptionEnfantDto());

        // Act
        underTest.reinscription(reinscriptionDto);

        // Assert — l'entité persistée porte le flag réinscription à true
        ArgumentCaptor<InscriptionEnfantEntity> captor = ArgumentCaptor.forClass(InscriptionEnfantEntity.class);
        verify(inscriptionEnfantRepository).save(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getReinscription());
    }

    @Test
    public void testCreateInscription_FlagReinscriptionFalse() {
        // Une inscription normale (hors processus de réinscription) doit persister reinscription=false, pas null.
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(2);
        inscriptionEnfantDto.getResponsableLegal().setEmail("test@example.com");
        inscriptionEnfantDto.getResponsableLegal().setNom("Dupont");
        inscriptionEnfantDto.getResponsableLegal().setPrenom("Jean");

        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(2);

        UserDto createdUserDto = new UserDto();
        createdUserDto.setId(1L);

        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(this.userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);

        // Act
        this.underTest.createInscription(inscriptionEnfantDto);

        // Assert
        ArgumentCaptor<InscriptionEnfantEntity> captor = ArgumentCaptor.forClass(InscriptionEnfantEntity.class);
        verify(inscriptionEnfantRepository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getReinscription());
    }

    @Test
    public void testReinscription_InscriptionsDisabled() {
        // Arrange
        when(paramService.isInscriptionEnfantEnabled()).thenReturn(false);

        ReinscriptionDto reinscriptionDto = new ReinscriptionDto();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> underTest.reinscription(reinscriptionDto));
    }

    @Test
    public void testReinscription_NoElevesSelected() {
        // Arrange
        when(paramService.isInscriptionEnfantEnabled()).thenReturn(true);
        when(paramService.isReinscriptionPrioritaireEnabled()).thenReturn(true);

        ReinscriptionDto reinscriptionDto = new ReinscriptionDto();
        reinscriptionDto.setEleves(new ArrayList<>());
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> underTest.reinscription(reinscriptionDto));
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument peuplé dans findInscriptionById
    // ---------------------------------------------------------------------------

    @Test
    public void testFindInscriptionById_IdDocumentPeuple_QuandDocumentTrouve() {
        // Arrange
        Long id = 10L;
        InscriptionEnfantEntity entity = new InscriptionEnfantEntity();
        entity.setId(id);
        entity.setResponsableLegal(new ResponsableLegalEntity());
        entity.setEleves(new ArrayList<>());

        DocumentEntity doc = new DocumentEntity();
        doc.setId(55L);

        SituationPaiementDto situation = SituationPaiementDto.builder().idCible(id).build();

        when(inscriptionEnfantRepository.findById(id)).thenReturn(Optional.of(entity));
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKeyEnum.ID_INSCRIPTION), eq(String.valueOf(id))))
                .thenReturn(Optional.of(doc));
        when(paiementService.getSituation(TypeCiblePaiementEnum.INSCRIPTION, id)).thenReturn(situation);

        // Act
        InscriptionEnfantDto result = underTest.findInscriptionById(id);

        // Assert
        assertNotNull(result);
        assertEquals(55L, result.getIdDocument());
        // La situation de règlement est embarquée pour éviter au front un second appel
        assertSame(situation, result.getSituationPaiement());
    }

    @Test
    public void testFindInscriptionById_IdDocumentNull_QuandAucunDocument() {
        // Arrange
        Long id = 10L;
        InscriptionEnfantEntity entity = new InscriptionEnfantEntity();
        entity.setId(id);
        entity.setResponsableLegal(new ResponsableLegalEntity());
        entity.setEleves(new ArrayList<>());

        when(inscriptionEnfantRepository.findById(id)).thenReturn(Optional.of(entity));
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKeyEnum.ID_INSCRIPTION), eq(String.valueOf(id))))
                .thenReturn(Optional.empty());

        // Act
        InscriptionEnfantDto result = underTest.findInscriptionById(id);

        // Assert
        assertNotNull(result);
        assertNull(result.getIdDocument());
    }

    @Test
    public void testFindInscriptionById_RetourneNull_QuandInscriptionAbsente() {
        // Arrange
        when(inscriptionEnfantRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        InscriptionEnfantDto result = underTest.findInscriptionById(99L);

        // Assert
        assertNull(result);
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument peuplé dans updateInscription
    // ---------------------------------------------------------------------------

    @Test
    public void testUpdateInscription_IdDocumentPeuple_QuandDocumentTrouve() {
        // Arrange
        Long id = 1L;
        // 0 élèves pour éviter l'appel à eleveMapper dans le Spy (champ final null à la construction)
        InscriptionEnfantDto inscriptionDto = createInscription(0);
        inscriptionDto.getResponsableLegal().setEmail("test@example.com");
        InscriptionEnfantEntity entity = createInscriptionEntityWithDate(0);
        entity.setId(id);

        DocumentEntity doc = new DocumentEntity();
        doc.setId(66L);

        when(inscriptionEnfantRepository.findById(id)).thenReturn(Optional.of(entity));
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionEnfantRepository.save(any())).thenReturn(entity);
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKeyEnum.ID_INSCRIPTION), eq(String.valueOf(id))))
                .thenReturn(Optional.of(doc));

        // Act
        InscriptionEnfantDto result = underTest.updateInscription(id, inscriptionDto,
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        // Assert
        assertNotNull(result);
        assertEquals(66L, result.getIdDocument());
    }

    @Test
    public void testUpdateInscription_IdDocumentNull_QuandAucunDocument() {
        // Arrange
        Long id = 1L;
        // 0 élèves pour éviter l'appel à eleveMapper dans le Spy (champ final null à la construction)
        InscriptionEnfantDto inscriptionDto = createInscription(0);
        inscriptionDto.getResponsableLegal().setEmail("test@example.com");
        InscriptionEnfantEntity entity = createInscriptionEntityWithDate(0);
        entity.setId(id);

        when(inscriptionEnfantRepository.findById(id)).thenReturn(Optional.of(entity));
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionEnfantRepository.save(any())).thenReturn(entity);
        when(documentRepository.findByMetadataKeyAndValue(
                eq(DocumentMetadataKeyEnum.ID_INSCRIPTION), eq(String.valueOf(id))))
                .thenReturn(Optional.empty());

        // Act
        InscriptionEnfantDto result = underTest.updateInscription(id, inscriptionDto,
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        // Assert
        assertNotNull(result);
        assertNull(result.getIdDocument());
    }

    // ---------------------------------------------------------------------------
    // Tests idDocument dans reinscription
    // ---------------------------------------------------------------------------

    @Test
    public void testReinscription_IdDocumentNonPeuple_CarAsynchrone() {
        // La réinscription génère le document en asynchrone ;
        // le DTO retourné ne contient donc pas d'idDocument.
        String username = "testuser";
        UserDto utilisateur = new UserDto();
        utilisateur.setId(1L);

        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        periode.setIdPeriodePrecedente(0L);

        TarifEntity tarif = new TarifEntity();
        tarif.setId(1L);
        tarif.setPeriode(periode);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(1L);
        eleve.setNom("Dupont");
        eleve.setPrenom("Marie");
        eleve.setIdInscription(1L);
        eleve.setIdTarif(1L);

        ResponsableLegalEntity responsableLegalEntity = new ResponsableLegalEntity();
        InscriptionEnfantEntity ancienneInscription = new InscriptionEnfantEntity();
        ancienneInscription.setId(1L);
        ancienneInscription.setIdUtilisateur(utilisateur.getId());

        EleveReinscriptionDto eleveReinscription = EleveReinscriptionDto.builder()
                .id(1L).niveau(NiveauScolaireEnum.CP).build();

        ReinscriptionDto reinscriptionDto = new ReinscriptionDto();
        reinscriptionDto.setEleves(List.of(eleveReinscription));
        reinscriptionDto.setResponsableLegal(ResponsableLegalDto.builder().build());

        InscriptionEnfantDto inscriptionDto = new InscriptionEnfantDto();

        when(paramService.isInscriptionEnfantEnabled()).thenReturn(true);
        when(paramService.isReinscriptionPrioritaireEnabled()).thenReturn(true);
        when(securityContext.getUser()).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(eleveRepository.findAllById(List.of(1L))).thenReturn(List.of(eleve));
        when(inscriptionEnfantRepository.findById(1L)).thenReturn(Optional.of(ancienneInscription));
        when(responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal()))
                .thenReturn(responsableLegalEntity);
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));
        when(inscriptionRepository.findFirstEleveByNomPrenomDateNaissanceIdPeriode(any(), any(), any(), any()))
                .thenReturn(eleve);
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        InscriptionEnfantEntity savedInscription = new InscriptionEnfantEntity();
        savedInscription.setStatut(StatutInscriptionEnum.VALIDEE);
        when(inscriptionEnfantRepository.save(any())).thenReturn(savedInscription);
        when(inscriptionEnfantMapper.fromEntityToDto(any())).thenReturn(inscriptionDto);

        // Act
        InscriptionEnfantDto result = underTest.reinscription(reinscriptionDto);

        // Assert — idDocument non peuplé car la génération est asynchrone
        assertNotNull(result);
        assertNull(result.getIdDocument());
        verify(asyncDocumentService, times(1))
                .requestDocumentGeneration(eq(DocumentRequestTypeEnum.INSCRIPTION_ENFANT), any());
    }

    // ---------------------------------------------------------------------------
    // Tests conditionnement de la génération de document selon le statut
    // ---------------------------------------------------------------------------

    @Test
    public void testCreateInscription_DocumentGenere_QuandStatutProvisoire() {
        // GIVEN
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(1);
        inscriptionEnfantDto.getResponsableLegal().setEmail("test@example.com");
        inscriptionEnfantDto.getResponsableLegal().setNom("Dupont");
        inscriptionEnfantDto.getResponsableLegal().setPrenom("Jean");

        // L'entité aura statut PROVISOIRE (défini par computeStatutNewInscription car listeAttente=false)
        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(1);

        UserDto createdUserDto = new UserDto();
        createdUserDto.setId(1L);

        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(this.userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);

        // WHEN
        InscriptionEnfantResultDto result = this.underTest.createInscription(inscriptionEnfantDto);

        // THEN
        assertNotNull(result);
        assertEquals(StatutInscriptionEnum.PROVISOIRE, result.getStatut());
        verify(asyncDocumentService, times(1))
                .requestDocumentGeneration(eq(DocumentRequestTypeEnum.INSCRIPTION_ENFANT), any());
        verify(mailRequestRepository).save(any());
    }

    @Test
    public void testUpdateInscription_DocumentNonGenere_QuandStatutRefuse() {
        // GIVEN — save retourne une entité avec statut REFUSE : la condition PROVISOIRE || VALIDEE
        // est fausse, donc requestDocumentGeneration ne doit pas être appelé.
        Long id = 1L;
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(0);
        inscriptionEnfantDto.getResponsableLegal().setEmail("test@example.com");

        InscriptionEnfantEntity entityRefuse = createInscriptionEntityWithDate(0);
        entityRefuse.setId(id);
        entityRefuse.setStatut(StatutInscriptionEnum.REFUSE);

        when(inscriptionEnfantRepository.findById(id)).thenReturn(Optional.of(entityRefuse));
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionEnfantRepository.save(any())).thenReturn(entityRefuse);

        // WHEN
        InscriptionEnfantDto result = underTest.updateInscription(id, inscriptionEnfantDto,
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        // THEN
        assertNotNull(result);
        verify(asyncDocumentService, never()).requestDocumentGeneration(any(), any());
        verify(mailRequestRepository, never()).save(any());
    }

    @Test
    public void testCreateInscription_DocumentNonGenere_QuandStatutListeAttente() {
        // GIVEN
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(1);
        inscriptionEnfantDto.getResponsableLegal().setEmail("test@example.com");
        inscriptionEnfantDto.getResponsableLegal().setNom("Dupont");
        inscriptionEnfantDto.getResponsableLegal().setPrenom("Jean");

        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(1);

        UserDto createdUserDto = new UserDto();
        createdUserDto.setId(1L);

        // listeAttente=true : computeStatutNewInscription positionne LISTE_ATTENTE sur l'entité
        TarifInscriptionEnfantDto tarifListeAttente = TarifInscriptionEnfantDto.builder()
                .idTariEleve(1L).idTariBase(2L)
                .tarifEleve(BigDecimal.valueOf(12)).tarifBase(BigDecimal.valueOf(165))
                .listeAttente(Boolean.TRUE).build();

        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(this.userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(tarifListeAttente);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        when(this.inscriptionEnfantRepository.getLastPositionAttente(any(LocalDate.class))).thenReturn(null);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);

        // WHEN
        InscriptionEnfantResultDto result = this.underTest.createInscription(inscriptionEnfantDto);

        // THEN
        assertNotNull(result);
        assertEquals(StatutInscriptionEnum.LISTE_ATTENTE, result.getStatut());
        verify(asyncDocumentService, never())
                .requestDocumentGeneration(any(), any());
        verify(mailRequestRepository, times(1)).save(any());
    }

    // ---------------------------------------------------------------------------
    // Transitions de statut (checkStatutInscription) via updateInscription
    // ---------------------------------------------------------------------------

    /**
     * Prépare un updateInscription dont le mapper positionne {@code nouveauStatut} sur l'entité,
     * en simulant un statut initial {@code ancienStatut}.
     */
    private InscriptionEnfantEntity prepareUpdateAvecChangementStatut(StatutInscriptionEnum ancienStatut,
                                                                     StatutInscriptionEnum nouveauStatut) {
        InscriptionEnfantEntity entity = createInscriptionEntityWithDate(0);
        entity.setId(1L);
        entity.setStatut(ancienStatut);

        doAnswer(invocation -> {
            InscriptionEnfantEntity target = invocation.getArgument(1);
            target.setStatut(nouveauStatut);
            return null;
        }).when(inscriptionEnfantMapper).updateInscriptionEntity(any(), any());

        when(inscriptionEnfantRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionEnfantRepository.save(any())).thenReturn(entity);
        return entity;
    }

    @Test
    public void testUpdateInscription_SortieDeListeAttente_ReinitialiseLaPosition() {
        InscriptionEnfantEntity entity = prepareUpdateAvecChangementStatut(
                StatutInscriptionEnum.LISTE_ATTENTE, StatutInscriptionEnum.PROVISOIRE);
        entity.setNoPositionAttente(5);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        assertNull(entity.getNoPositionAttente());
    }

    @Test
    public void testUpdateInscription_PassageEnListeAttente_CalculeLaPosition() {
        InscriptionEnfantEntity entity = prepareUpdateAvecChangementStatut(
                StatutInscriptionEnum.PROVISOIRE, StatutInscriptionEnum.LISTE_ATTENTE);
        when(inscriptionEnfantRepository.getLastPositionAttente(any(LocalDate.class))).thenReturn(7);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        assertEquals(8, entity.getNoPositionAttente());
    }

    @Test
    public void testUpdateInscription_PremierePositionEnListeAttente() {
        InscriptionEnfantEntity entity = prepareUpdateAvecChangementStatut(
                StatutInscriptionEnum.VALIDEE, StatutInscriptionEnum.LISTE_ATTENTE);
        when(inscriptionEnfantRepository.getLastPositionAttente(any(LocalDate.class))).thenReturn(null);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        assertEquals(1, entity.getNoPositionAttente());
    }

    @Test
    public void testUpdateInscription_StatutInchange_NeTouchePasALaPosition() {
        InscriptionEnfantEntity entity = prepareUpdateAvecChangementStatut(
                StatutInscriptionEnum.PROVISOIRE, StatutInscriptionEnum.PROVISOIRE);
        entity.setNoPositionAttente(3);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        assertEquals(3, entity.getNoPositionAttente());
        verify(inscriptionEnfantRepository, never()).getLastPositionAttente(any(LocalDate.class));
    }

    @Test
    public void testUpdateInscription_DepuisRefuseVersValidee_NeCalculePasDePosition() {
        InscriptionEnfantEntity entity = prepareUpdateAvecChangementStatut(
                StatutInscriptionEnum.REFUSE, StatutInscriptionEnum.VALIDEE);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        assertNull(entity.getNoPositionAttente());
        verify(inscriptionEnfantRepository, never()).getLastPositionAttente(any(LocalDate.class));
    }

    @Test
    public void testUpdateInscription_EnvoieLeMailDeConfirmationQuandDemande() {
        prepareUpdateAvecChangementStatut(StatutInscriptionEnum.LISTE_ATTENTE, StatutInscriptionEnum.VALIDEE);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(true).build());

        verify(asyncDocumentService).requestDocumentGeneration(eq(DocumentRequestTypeEnum.INSCRIPTION_ENFANT), eq(1L));
        verify(mailRequestRepository).save(any());
    }

    @Test
    public void testUpdateInscription_PasDeMailQuandNonDemande() {
        prepareUpdateAvecChangementStatut(StatutInscriptionEnum.LISTE_ATTENTE, StatutInscriptionEnum.VALIDEE);

        underTest.updateInscription(1L, createInscriptionNormalisable(0),
                InscriptionSaveCriteria.builder().sendMailConfirmation(false).build());

        verify(asyncDocumentService).requestDocumentGeneration(eq(DocumentRequestTypeEnum.INSCRIPTION_ENFANT), eq(1L));
        verify(mailRequestRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------------
    // Réinscription : validation des élèves et calcul du nouveau niveau
    // ---------------------------------------------------------------------------

    /**
     * Prépare une réinscription nominale. {@code ancienEleve} est ce que la recherche sur la période
     * précédente renvoie (null = élève non reconnu).
     */
    private ReinscriptionDto prepareReinscription(EleveEntity ancienEleve, InscriptionEnfantEntity savedInscription) {
        String username = "testuser";
        UserDto utilisateur = new UserDto();
        utilisateur.setId(1L);

        PeriodeEntity periode = new PeriodeEntity();
        periode.setId(1L);
        periode.setIdPeriodePrecedente(0L);

        TarifEntity tarif = new TarifEntity();
        tarif.setId(1L);
        tarif.setPeriode(periode);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(1L);
        eleve.setNom("Dupont");
        eleve.setPrenom("Marie");
        eleve.setIdInscription(1L);
        eleve.setIdTarif(1L);

        InscriptionEnfantEntity ancienneInscription = new InscriptionEnfantEntity();
        ancienneInscription.setId(1L);
        ancienneInscription.setIdUtilisateur(utilisateur.getId());

        ReinscriptionDto reinscriptionDto = new ReinscriptionDto();
        reinscriptionDto.setEleves(List.of(EleveReinscriptionDto.builder().id(1L).niveau(NiveauScolaireEnum.CP).build()));
        reinscriptionDto.setResponsableLegal(ResponsableLegalDto.builder().build());

        when(paramService.isInscriptionEnfantEnabled()).thenReturn(true);
        when(paramService.isReinscriptionPrioritaireEnabled()).thenReturn(true);
        when(securityContext.getUser()).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(eleveRepository.findAllById(List.of(1L))).thenReturn(List.of(eleve));
        when(inscriptionEnfantRepository.findById(1L)).thenReturn(Optional.of(ancienneInscription));
        when(responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal())).thenReturn(new ResponsableLegalEntity());
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));
        when(inscriptionRepository.findFirstEleveByNomPrenomDateNaissanceIdPeriode(any(), any(), any(), any()))
                .thenReturn(ancienEleve);
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        when(inscriptionEnfantRepository.save(any())).thenReturn(savedInscription);
        when(inscriptionEnfantMapper.fromEntityToDto(any())).thenReturn(new InscriptionEnfantDto());

        return reinscriptionDto;
    }

    @Test
    public void testReinscription_EleveNonReconnu_InscriptionRefusee() {
        InscriptionEnfantEntity saved = new InscriptionEnfantEntity();
        saved.setStatut(StatutInscriptionEnum.REFUSE);
        // Aucun élève retrouvé sur la période précédente => la réinscription est refusée
        ReinscriptionDto reinscriptionDto = prepareReinscription(null, saved);

        underTest.reinscription(reinscriptionDto);

        ArgumentCaptor<InscriptionEnfantEntity> captor = ArgumentCaptor.forClass(InscriptionEnfantEntity.class);
        verify(inscriptionEnfantRepository).save(captor.capture());
        assertEquals(StatutInscriptionEnum.REFUSE, captor.getValue().getStatut());
        // Statut REFUSE => pas de génération de document
        verify(asyncDocumentService, never()).requestDocumentGeneration(any(), any());
    }

    @Test
    public void testReinscription_AnneeNonAcquise_EleveResteDansSonNiveau() {
        EleveEntity ancienEleve = new EleveEntity();
        ancienEleve.setId(1L);
        ancienEleve.setNiveauInterne(NiveauInterneEnum.N1_1);
        ancienEleve.setResultat(ResultatEnum.NON_ACQUIS);

        InscriptionEnfantEntity saved = new InscriptionEnfantEntity();
        saved.setStatut(StatutInscriptionEnum.VALIDEE);

        underTest.reinscription(prepareReinscription(ancienEleve, saved));

        ArgumentCaptor<InscriptionEnfantEntity> captor = ArgumentCaptor.forClass(InscriptionEnfantEntity.class);
        verify(inscriptionEnfantRepository).save(captor.capture());
        assertEquals(NiveauInterneEnum.N1_1, captor.getValue().getEleves().get(0).getNiveauInterne());
        verify(niveauRepository, never()).findNiveauSuperieurByNiveau(any());
    }

    @Test
    public void testReinscription_AnneeAcquise_ElevePasseAuNiveauSuperieur() {
        EleveEntity ancienEleve = new EleveEntity();
        ancienEleve.setId(1L);
        ancienEleve.setNiveauInterne(NiveauInterneEnum.N1_1);
        ancienEleve.setResultat(ResultatEnum.ACQUIS);

        InscriptionEnfantEntity saved = new InscriptionEnfantEntity();
        saved.setStatut(StatutInscriptionEnum.VALIDEE);

        when(niveauRepository.findNiveauSuperieurByNiveau(NiveauInterneEnum.N1_1)).thenReturn(NiveauInterneEnum.N1_2);

        underTest.reinscription(prepareReinscription(ancienEleve, saved));

        ArgumentCaptor<InscriptionEnfantEntity> captor = ArgumentCaptor.forClass(InscriptionEnfantEntity.class);
        verify(inscriptionEnfantRepository).save(captor.capture());
        assertEquals(NiveauInterneEnum.N1_2, captor.getValue().getEleves().get(0).getNiveauInterne());
    }

    @Test
    public void testReinscription_ResultatInconnu_NiveauNonCalcule() {
        // Ancien élève sans niveau ni résultat : impossible de déterminer le nouveau niveau
        EleveEntity ancienEleve = new EleveEntity();
        ancienEleve.setId(1L);

        InscriptionEnfantEntity saved = new InscriptionEnfantEntity();
        saved.setStatut(StatutInscriptionEnum.VALIDEE);

        underTest.reinscription(prepareReinscription(ancienEleve, saved));

        ArgumentCaptor<InscriptionEnfantEntity> captor = ArgumentCaptor.forClass(InscriptionEnfantEntity.class);
        verify(inscriptionEnfantRepository).save(captor.capture());
        assertNull(captor.getValue().getEleves().get(0).getNiveauInterne());
        verify(niveauRepository, never()).findNiveauSuperieurByNiveau(any());
    }

    // ---------------------------------------------------------------------------
    // Détection de doublons d'élèves (checkCoherence)
    // ---------------------------------------------------------------------------

    @Test
    public void testCheckCoherence_AucunEleve() {
        InscriptionEnfantDto dto = createInscriptionNormalisable(0);

        assertEquals(Incoherences.NO_INCOHERENCE, underTest.checkCoherence(null, dto));
        verify(inscriptionEnfantRepository, never()).findInscriptionsWithEleve(any(), any(), any(), any(), any());
    }

    @Test
    public void testCheckCoherence_EleveDejaInscrit() {
        InscriptionEnfantDto dto = new InscriptionEnfantDto();
        dto.setResponsableLegal(ResponsableLegalDto.builder().email("test@example.com").build());
        dto.setEleves(new ArrayList<>(List.of(
                EleveDto.builder().nom("Dupont").prenom("Marie").dateNaissance(LocalDate.of(2015, 5, 1)).build())));

        when(inscriptionEnfantRepository.findInscriptionsWithEleve(eq("Marie"), eq("Dupont"),
                eq(LocalDate.of(2015, 5, 1)), any(LocalDate.class), isNull()))
                .thenReturn(List.of(new InscriptionEnfantEntity()));

        assertEquals(Incoherences.ELEVE_ALREADY_EXISTS, underTest.checkCoherence(null, dto));
    }

    @Test
    public void testCheckCoherence_EleveNonInscrit() {
        InscriptionEnfantDto dto = new InscriptionEnfantDto();
        dto.setResponsableLegal(ResponsableLegalDto.builder().email("test@example.com").build());
        dto.setEleves(new ArrayList<>(List.of(
                EleveDto.builder().nom("Dupont").prenom("Marie").build())));

        when(inscriptionEnfantRepository.findInscriptionsWithEleve(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        assertEquals(Incoherences.NO_INCOHERENCE, underTest.checkCoherence(null, dto));
    }

    @Test
    public void testCheckCoherence_UtiliseLaDateDeLInscriptionExistante() {
        // Sur une modification, la recherche de doublon doit se faire à la date de l'inscription
        // en cours de modification, pas à la date du jour.
        LocalDateTime dateInscription = LocalDateTime.of(2024, 9, 15, 10, 0);
        InscriptionEnfantEntity existante = new InscriptionEnfantEntity();
        existante.setId(42L);
        existante.setDateInscription(dateInscription);

        InscriptionEnfantDto dto = new InscriptionEnfantDto();
        dto.setResponsableLegal(ResponsableLegalDto.builder().email("test@example.com").build());
        dto.setEleves(new ArrayList<>(List.of(
                EleveDto.builder().nom("Dupont").prenom("Marie").build())));

        when(inscriptionEnfantRepository.findById(42L)).thenReturn(Optional.of(existante));
        when(inscriptionEnfantRepository.findInscriptionsWithEleve(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        assertEquals(Incoherences.NO_INCOHERENCE, underTest.checkCoherence(42L, dto));

        verify(inscriptionEnfantRepository).findInscriptionsWithEleve(eq("Marie"), eq("Dupont"),
                isNull(), eq(dateInscription.toLocalDate()), eq(42L));
    }

    @Test
    public void testCheckCoherence_InscriptionAModifierIntrouvable_UtiliseLaDateDuJour() {
        InscriptionEnfantDto dto = new InscriptionEnfantDto();
        dto.setResponsableLegal(ResponsableLegalDto.builder().email("test@example.com").build());
        dto.setEleves(new ArrayList<>(List.of(
                EleveDto.builder().nom("Dupont").prenom("Marie").build())));

        when(inscriptionEnfantRepository.findById(99L)).thenReturn(Optional.empty());
        when(inscriptionEnfantRepository.findInscriptionsWithEleve(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        assertEquals(Incoherences.NO_INCOHERENCE, underTest.checkCoherence(99L, dto));

        verify(inscriptionEnfantRepository).findInscriptionsWithEleve(eq("Marie"), eq("Dupont"),
                isNull(), eq(AUJOURD_HUI), eq(99L));
    }

    @Test
    public void testCheckCoherence_EleveSansNomNiPrenom_Ignore() {
        InscriptionEnfantDto dto = new InscriptionEnfantDto();
        dto.setResponsableLegal(ResponsableLegalDto.builder().email("test@example.com").build());
        dto.setEleves(new ArrayList<>(List.of(EleveDto.builder().build())));

        assertEquals(Incoherences.NO_INCOHERENCE, underTest.checkCoherence(null, dto));
        verify(inscriptionEnfantRepository, never()).findInscriptionsWithEleve(any(), any(), any(), any(), any());
    }

    // ---------------------------------------------------------------------------
    // Comptages et contrôle de période
    // ---------------------------------------------------------------------------

    @Test
    public void testFindNbInscriptionsByPeriode() {
        when(inscriptionRepository.getNbElevesInscritsByIdPeriode(1L, TypeInscriptionEnum.ENFANT.name())).thenReturn(12);

        assertEquals(12, underTest.findNbInscriptionsByPeriode(1L));
    }

    @Test
    public void testGetNbElevesInscritsByIdPeriode() {
        when(inscriptionRepository.getNbElevesInscritsByIdPeriode(1L, TypeInscriptionEnum.ENFANT.name())).thenReturn(30);

        assertEquals(30, underTest.getNbElevesInscritsByIdPeriode(1L));
    }

    @Test
    public void testIsInscriptionOutsidePeriode_VraiSiAuMoinsUneInscriptionHorsPeriode() {
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setDateDebut(LocalDate.of(2024, 9, 1));
        periodeDto.setDateFin(LocalDate.of(2025, 6, 30));
        when(inscriptionRepository.getNbInscriptionOutsideRange(1L, periodeDto.getDateDebut(), periodeDto.getDateFin(),
                TypeInscriptionEnum.ENFANT.name())).thenReturn(3);

        assertTrue(underTest.isInscriptionOutsidePeriode(1L, periodeDto));
    }

    @Test
    public void testIsInscriptionOutsidePeriode_FauxSiAucune() {
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setDateDebut(LocalDate.of(2024, 9, 1));
        periodeDto.setDateFin(LocalDate.of(2025, 6, 30));
        when(inscriptionRepository.getNbInscriptionOutsideRange(any(), any(), any(), any())).thenReturn(0);

        assertFalse(underTest.isInscriptionOutsidePeriode(1L, periodeDto));
    }

    @Test
    public void testIsInscriptionOutsidePeriode_FauxSiComptageNull() {
        PeriodeDto periodeDto = new PeriodeDto();
        when(inscriptionRepository.getNbInscriptionOutsideRange(any(), any(), any(), any())).thenReturn(null);

        assertFalse(underTest.isInscriptionOutsidePeriode(1L, periodeDto));
    }

}
