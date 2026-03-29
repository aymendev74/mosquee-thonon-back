package org.mosqueethonon.service.impl.inscription;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
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
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inscriptionAdulteService.updateInscription(1L, inscriptionDto, criteria);
        });

        assertEquals("Inscription not found ! idinsc = 1", exception.getMessage());
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
    public void testReinscription_InscriptionClosed() {
        // Arrange
        when(paramService.isInscriptionAdulteEnabled()).thenReturn(false);

        ReinscriptionAdulteDto reinscriptionDto = new ReinscriptionAdulteDto();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> inscriptionAdulteService.reinscription(reinscriptionDto));
    }
}
