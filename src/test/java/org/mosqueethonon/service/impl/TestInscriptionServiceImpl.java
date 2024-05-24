package org.mosqueethonon.service.impl;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.v1.dto.*;
import org.mosqueethonon.v1.mapper.InscriptionMapper;

import java.math.BigDecimal;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionServiceImpl {

    @Mock
    private InscriptionRepository inscriptionRepository;
    @Mock
    private InscriptionMapper inscriptionMapper;
    @Mock
    private TarifCalculService tarifCalculService;
    @Mock
    private MailingConfirmationRepository mailingConfirmationRepository;
    @Mock
    private PeriodeRepository periodeRepository;
    @Mock
    private ParamService paramService;
    @InjectMocks
    private InscriptionServiceImpl underTest;

    @Test
    public void testSaveInscriptionSendEmailConfirmation() {
        this.testSaveInscription(Boolean.TRUE);
    }

    @Test
    public void testSaveInscriptionDoNotSendEmailConfirmation() {
        this.testSaveInscription(Boolean.FALSE);
    }

    private void testSaveInscription(boolean sendMailConfirmation) {
        // GIVEN
        final String anneeScolaire = "2024/2025";
        final Long numeroInscription = Long.valueOf(1001);
        final InscriptionDto inscriptionDto = createInscription(2);
        final InscriptionEntity inscriptionEntity = new InscriptionEntity();
        when(this.tarifCalculService.calculTarifInscription(any())).thenReturn(createTarifInscription());
        when(this.paramService.isReinscriptionPrioritaireEnabled()).thenReturn(Boolean.FALSE);
        when(this.inscriptionMapper.fromEntityToDto(any())).thenReturn(inscriptionDto);
        when(this.inscriptionMapper.fromDtoToEntity(any())).thenReturn(inscriptionEntity);
        when(this.paramService.getAnneeScolaireEnCours()).thenReturn(anneeScolaire);
        when(this.inscriptionRepository.getNextNumeroInscription()).thenReturn(numeroInscription);
        when(this.inscriptionRepository.save(any())).thenReturn(inscriptionEntity);

        // WHEN
        this.underTest.saveInscription(inscriptionDto, InscriptionSaveCriteria.builder().sendMailConfirmation(sendMailConfirmation)
                .build());

        // THEN
        verify(this.mailingConfirmationRepository, times(sendMailConfirmation ? 1 : 0)).save(any());
        assertEquals(anneeScolaire, inscriptionEntity.getAnneeScolaire());
        assertEquals(new StringBuilder("AMC-").append(numeroInscription).toString(), inscriptionEntity.getNoInscription());
        assertNotNull(inscriptionEntity.getDateInscription());
        assertEquals(BigDecimal.valueOf(189), inscriptionDto.getMontantTotal());
    }

    private TarifInscriptionDto createTarifInscription() {
        return TarifInscriptionDto.builder().idTariEleve(1L).idTariBase(2L)
                .tarifEleve(BigDecimal.valueOf(12)).tarifBase(BigDecimal.valueOf(165))
                .listeAttente(Boolean.FALSE).build();
    }

    private InscriptionDto createInscription(int nbEleves) {
        InscriptionDto inscriptionDto = new InscriptionDto();
        inscriptionDto.setResponsableLegal(ResponsableLegalDto.builder().adherent(Boolean.TRUE).build());
        inscriptionDto.setEleves(new ArrayList<>());
        for(int i = 0; i < nbEleves ; i++) {
            inscriptionDto.getEleves().add(EleveDto.builder().build());
        }
        return inscriptionDto;
    }
}
