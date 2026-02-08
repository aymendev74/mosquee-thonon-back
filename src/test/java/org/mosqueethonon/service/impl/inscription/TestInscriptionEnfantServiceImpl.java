package org.mosqueethonon.service.impl.inscription;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.*;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.*;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.inscription.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private EleveRepository eleveRepository;
    @InjectMocks
    private InscriptionEnfantServiceImpl underTest;

    @Test
    public void testSaveInscriptionExpectIllegalStateExceptionWhenInscriptionDisabled() {
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.FALSE);
        assertThrows(IllegalStateException.class,
                () -> {
                    this.underTest.createInscription(null);
                });
    }

    @Test
    private void testCreateInscription() {
        // GIVEN
        final String anneeScolaire = "2024/2025";
        final Long numeroInscription = Long.valueOf(1001);
        InscriptionEnfantDto inscriptionEnfantDto = createInscription(2);
        final InscriptionEnfantEntity inscriptionEnfantEntity = createInscriptionEntity(2);
        when(this.tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionEnfantMapper.fromEntityToDto(any())).thenReturn(inscriptionEnfantDto);
        when(this.inscriptionEnfantMapper.fromDtoToEntity(any())).thenReturn(inscriptionEnfantEntity);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(numeroInscription);
        when(this.inscriptionEnfantRepository.save(any())).thenReturn(inscriptionEnfantEntity);
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.TRUE);

        // WHEN
        this.underTest.createInscription(inscriptionEnfantDto);

        // THEN
        verify(this.mailRequestRepository).save(any());
        verify(this.inscriptionEnfantRepository).save(any());
        verify(this.paramService).isInscriptionEnfantEnabled();
        verify(this.inscriptionRepository).getNextNumeroInscription();
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

    private TarifInscriptionEnfantDto createTarifInscription() {
        return TarifInscriptionEnfantDto.builder().idTariEleve(1L).idTariBase(2L)
                .tarifEleve(BigDecimal.valueOf(12)).tarifBase(BigDecimal.valueOf(165))
                .listeAttente(Boolean.FALSE).build();
    }

    private InscriptionEnfantDto createInscription(int nbEleves) {
        InscriptionEnfantDto inscriptionEnfantDto = new InscriptionEnfantDto();
        inscriptionEnfantDto.setAdherent(Boolean.TRUE);
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
        UtilisateurEntity utilisateur = new UtilisateurEntity();
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
        inscription.setStatut(StatutInscription.VALIDEE);
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
        when(utilisateurRepository.findByUsername(username)).thenReturn(Optional.of(utilisateur));
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
        assertEquals(StatutInscription.VALIDEE, result.get(0).getStatut());
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
        when(utilisateurRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> underTest.findInscriptionsByUtilisateurConnecte());
    }

    @Test
    public void testReinscription_Success() {
        // Arrange
        String username = "testuser";
        UtilisateurEntity utilisateur = new UtilisateurEntity();
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
        ancienneInscription.setUtilisateur(utilisateur);

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
        when(utilisateurRepository.findByUsername(username)).thenReturn(Optional.of(utilisateur));
        when(eleveRepository.findAllById(List.of(1L))).thenReturn(List.of(eleve));
        when(inscriptionEnfantRepository.findById(1L)).thenReturn(Optional.of(ancienneInscription));
        when(responsableLegalMapper.fromDtoToEntity(reinscriptionDto.getResponsableLegal())).thenReturn(responsableLegalReinscription);
        when(tarifRepository.findById(1L)).thenReturn(Optional.of(tarif));
        when(inscriptionRepository.findFirstEleveByNomPrenomDateNaissanceIdPeriode(any(), any(), any(), any())).thenReturn(eleve);
        when(tarifCalculService.calculTarifInscriptionEnfant(any(), any())).thenReturn(createTarifInscription());
        when(inscriptionRepository.getNextNumeroInscription()).thenReturn(1001L);
        when(inscriptionEnfantRepository.save(any())).thenReturn(new InscriptionEnfantEntity());
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

}
