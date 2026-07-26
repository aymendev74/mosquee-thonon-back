package org.mosqueethonon.param.service.impl;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.config.APIDateFormats;
import org.mosqueethonon.param.entity.ParamEntity;
import org.mosqueethonon.param.enums.ParamNameEnum;
import org.mosqueethonon.param.parser.BooleanParamValueParser;
import org.mosqueethonon.param.parser.DateParamValueParser;
import org.mosqueethonon.param.parser.ParamParser;
import org.mosqueethonon.param.repository.ParamRepository;
import org.mosqueethonon.param.v1.dto.ParamDto;
import org.mosqueethonon.param.v1.dto.ParamsDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TestParamServiceImpl {

    @Mock
    private ParamRepository paramRepository;

    @InjectMocks
    private ParamServiceImpl underTest;

    @Mock
    private DateParamValueParser dateParamValueParser;

    @Mock
    private BooleanParamValueParser booleanParamValueParser;

    @Mock
    private ParamParser paramParser;

    @Test
    public void testIsInscriptionEnabledIsTrueEnfant() {
        // GIVEN
        ParamEntity param = new ParamEntity();
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE))).thenReturn(param);
        when(this.dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.parse("01.01.1950", DateTimeFormatter.ofPattern(APIDateFormats.DATE_FORMAT)));

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionEnfantEnabled();

        // THEN
        assertTrue(isInscriptionEnabled);
    }

    @Test
    public void testIsInscriptionEnabledIsFalseEnfant() {
        // GIVEN
        ParamEntity param = new ParamEntity();
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE))).thenReturn(param);
        when(this.dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.now().plusDays(1));

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionEnfantEnabled();

        // THEN
        assertFalse(isInscriptionEnabled);
    }

    @Test
    public void testIsInscriptionAdulteEnabledIsTrue() {
        // GIVEN
        ParamEntity param = new ParamEntity();
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ADULTE_ENABLED_FROM_DATE))).thenReturn(param);
        when(this.dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.parse("01.01.1950", DateTimeFormatter.ofPattern(APIDateFormats.DATE_FORMAT)));

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionAdulteEnabled();

        // THEN
        assertTrue(isInscriptionEnabled);
    }

    @Test
    public void testIsInscriptionAdulteEnabledIsFalse() {
        // GIVEN
        ParamEntity param = new ParamEntity();
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ADULTE_ENABLED_FROM_DATE))).thenReturn(param);
        when(this.dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.now().plusDays(1));

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionAdulteEnabled();

        // THEN
        assertFalse(isInscriptionEnabled);
    }

    @Test
    public void testIsInscriptionAdulteEnabledIsFalse_ParamNotFound() {
        // GIVEN
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ADULTE_ENABLED_FROM_DATE))).thenReturn(null);

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionAdulteEnabled();

        // THEN
        assertFalse(isInscriptionEnabled);
    }

    private ParamDto paramDto(ParamNameEnum name, String value) {
        ParamDto dto = new ParamDto();
        dto.setName(name);
        dto.setValue(value);
        return dto;
    }

    private ParamEntity paramEntity(ParamNameEnum name, String value) {
        ParamEntity entity = new ParamEntity();
        entity.setName(name);
        entity.setValue(value);
        return entity;
    }

    @Nested
    class QuandOnLitUnParametreDate {

        @Test
        public void testRetourneFauxQuandLaValeurEstVide() {
            // GIVEN — une date vide signifie « pas de date d'ouverture »
            when(paramRepository.findByName(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE))
                    .thenReturn(paramEntity(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE, ""));

            // WHEN / THEN
            assertFalse(underTest.isInscriptionEnfantEnabled());
            Mockito.verifyNoInteractions(dateParamValueParser);
        }

        @Test
        public void testOuvreLesInscriptionsLeJourMeme() {
            // GIVEN — cas limite : la date d'ouverture est aujourd'hui
            when(paramRepository.findByName(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE))
                    .thenReturn(new ParamEntity());
            when(dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.now());

            // WHEN / THEN
            assertTrue(underTest.isInscriptionEnfantEnabled());
        }
    }

    @Nested
    class QuandOnLitUnParametreBooleen {

        @Test
        public void testRetourneLaValeurParseeDuParametre() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "true"));
            when(booleanParamValueParser.getValue("true")).thenReturn(true);

            // WHEN / THEN
            assertTrue(underTest.isReinscriptionPrioritaireEnabled());
        }

        @Test
        public void testRetourneFauxQuandLeParametreEstAbsent() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.SEND_EMAIL_ENABLED)).thenReturn(null);

            // WHEN / THEN
            assertFalse(underTest.isSendEmailEnabled());
            Mockito.verifyNoInteractions(booleanParamValueParser);
        }

        @Test
        public void testLitLeParametreDEnvoiDeMail() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.SEND_EMAIL_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.SEND_EMAIL_ENABLED, "false"));
            when(booleanParamValueParser.getValue("false")).thenReturn(false);

            // WHEN / THEN
            assertFalse(underTest.isSendEmailEnabled());
        }
    }

    @Nested
    class QuandOnEnregistreDesParametres {

        @Test
        public void testMetAJourUnParametreExistant() {
            // GIVEN
            ParamEntity existant = paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "false");
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED)).thenReturn(existant);

            // WHEN
            underTest.saveParam(List.of(paramDto(ParamNameEnum.REINSCRIPTION_ENABLED, "true")));

            // THEN
            assertEquals("true", existant.getValue());
            verify(paramRepository).save(existant);
        }

        @Test
        public void testCreeUnParametreAbsentDeLaBase() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.SEND_EMAIL_ENABLED)).thenReturn(null);

            // WHEN
            underTest.saveParam(List.of(paramDto(ParamNameEnum.SEND_EMAIL_ENABLED, "true")));

            // THEN
            ArgumentCaptor<ParamEntity> captor = ArgumentCaptor.forClass(ParamEntity.class);
            verify(paramRepository).save(captor.capture());
            assertEquals(ParamNameEnum.SEND_EMAIL_ENABLED, captor.getValue().getName());
            assertEquals("true", captor.getValue().getValue());
        }

        @Test
        public void testRefuseUneValeurBooleenneInvalide() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "true"));

            // WHEN / THEN
            assertThrows(IllegalArgumentException.class,
                    () -> underTest.saveParam(List.of(paramDto(ParamNameEnum.REINSCRIPTION_ENABLED, "peut-etre"))));
            verify(paramRepository, never()).save(Mockito.any());
        }

        @Test
        public void testRefuseUneValeurBooleenneNulle() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "true"));

            // WHEN / THEN
            assertThrows(IllegalArgumentException.class,
                    () -> underTest.saveParam(List.of(paramDto(ParamNameEnum.REINSCRIPTION_ENABLED, null))));
        }

        @Test
        public void testAccepteTrueEtFalseSansTenirCompteDeLaCasse() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "false"));

            // WHEN
            underTest.saveParam(List.of(paramDto(ParamNameEnum.REINSCRIPTION_ENABLED, "TrUe")));

            // THEN
            verify(paramRepository).save(Mockito.any());
        }

        @Test
        public void testRefuseUneDateMalFormee() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE))
                    .thenReturn(paramEntity(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE, ""));

            // WHEN / THEN
            assertThrows(IllegalArgumentException.class, () -> underTest.saveParam(
                    List.of(paramDto(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE, "2025-01-01"))));
        }

        @Test
        public void testAccepteUneDateVidePourNeutraliserLOuverture() {
            // GIVEN
            ParamEntity existant = paramEntity(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE, "01.01.2025");
            when(paramRepository.findByName(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE))
                    .thenReturn(existant);

            // WHEN
            underTest.saveParam(List.of(paramDto(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE, "")));

            // THEN
            assertEquals("", existant.getValue());
        }

        @Test
        public void testSignaleUneValeurInvalideMemeSurUnParametreAbsentDeLaBase() {
            // GIVEN — le paramètre n'existe pas encore : le message d'erreur ne doit pas
            // déréférencer l'entité absente
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED)).thenReturn(null);

            // WHEN
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> underTest.saveParam(List.of(paramDto(ParamNameEnum.REINSCRIPTION_ENABLED, "invalide"))));

            // THEN
            assertTrue(exception.getMessage().contains("BOOLEAN"), exception.getMessage());
        }

        @Test
        public void testEnregistreTousLesParametresDeLaListe() {
            // GIVEN
            when(paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "false"));
            when(paramRepository.findByName(ParamNameEnum.SEND_EMAIL_ENABLED))
                    .thenReturn(paramEntity(ParamNameEnum.SEND_EMAIL_ENABLED, "false"));

            // WHEN
            underTest.saveParam(List.of(paramDto(ParamNameEnum.REINSCRIPTION_ENABLED, "true"),
                    paramDto(ParamNameEnum.SEND_EMAIL_ENABLED, "true")));

            // THEN
            verify(paramRepository, Mockito.times(2)).save(Mockito.any());
        }
    }

    @Nested
    class QuandOnLitTousLesParametres {

        @Test
        public void testRemplitLeDtoDepuisLesParametresAnnotes() {
            // GIVEN
            when(paramRepository.findAll()).thenReturn(List.of(
                    paramEntity(ParamNameEnum.REINSCRIPTION_ENABLED, "true"),
                    paramEntity(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE, "01.09.2025")));
            when(paramParser.parseValue("true", boolean.class)).thenReturn(true);
            when(paramParser.parseValue("01.09.2025", LocalDate.class))
                    .thenReturn(LocalDate.of(2025, 9, 1));

            // WHEN
            ParamsDto result = underTest.getParams();

            // THEN
            assertTrue(result.isReinscriptionPrioritaire());
            assertEquals(LocalDate.of(2025, 9, 1), result.getInscriptionEnfantEnabledFromDate());
        }

        @Test
        public void testLaisseLesChampsNonRenseignesAVideQuandAucunParametreNexiste() {
            // GIVEN
            when(paramRepository.findAll()).thenReturn(List.of());

            // WHEN
            ParamsDto result = underTest.getParams();

            // THEN
            assertFalse(result.isReinscriptionPrioritaire());
            assertNull(result.getInscriptionEnfantEnabledFromDate());
            Mockito.verifyNoInteractions(paramParser);
        }
    }
}
