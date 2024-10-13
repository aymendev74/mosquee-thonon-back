package org.mosqueethonon.service.impl.inscription;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.repository.InscriptionEnfantRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ResponsableLegalDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;
import org.mosqueethonon.v1.mapper.inscription.InscriptionEnfantMapper;

import java.math.BigDecimal;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionEnfantServiceImpl {

    @Mock
    private InscriptionEnfantRepository inscriptionEnfantRepository;
    @Mock
    private InscriptionRepository inscriptionRepository;
    @Mock
    private InscriptionEnfantMapper inscriptionEnfantMapper;
    @Mock
    private TarifCalculService tarifCalculService;
    @Mock
    private MailingConfirmationRepository mailingConfirmationRepository;
    @Mock
    private PeriodeRepository periodeRepository;
    @Mock
    private ParamService paramService;
    @InjectMocks
    private InscriptionEnfantServiceImpl underTest;

    @Test
    public void testSaveInscriptionSendEmailConfirmation() {
        this.testSaveInscription(Boolean.TRUE);
    }

    @Test
    public void testSaveInscriptionDoNotSendEmailConfirmation() {
        this.testSaveInscription(Boolean.FALSE);
    }

    @Test
    public void testSaveInscriptionExpectIllegalStateExceptionWhenInscriptionDisabled() {
        when(this.paramService.isInscriptionEnfantEnabled()).thenReturn(Boolean.FALSE);
        assertThrows(IllegalStateException.class,
                () -> {
                    this.underTest.createInscription(null, InscriptionSaveCriteria.builder()
                            .build());
                });
    }

    private void testSaveInscription(boolean sendMailConfirmation) {
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
        this.underTest.createInscription(inscriptionEnfantDto, InscriptionSaveCriteria.builder().sendMailConfirmation(sendMailConfirmation)
                .build());

        // THEN
        verify(this.mailingConfirmationRepository, times(sendMailConfirmation ? 1 : 0)).save(any());
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
        inscriptionEnfantDto.setResponsableLegal(ResponsableLegalDto.builder().adherent(Boolean.TRUE).build());
        inscriptionEnfantDto.setEleves(new ArrayList<>());
        for(int i = 0; i < nbEleves ; i++) {
            inscriptionEnfantDto.getEleves().add(EleveDto.builder().build());
        }
        return inscriptionEnfantDto;
    }
}
