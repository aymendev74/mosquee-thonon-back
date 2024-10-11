package org.mosqueethonon.service.impl.param;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.params.DateParamValueParser;
import org.mosqueethonon.repository.ParamRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
public class TestParamServiceImpl {

    @Mock
    private ParamRepository paramRepository;

    @InjectMocks
    private ParamServiceImpl underTest;

    @Mock
    private DateParamValueParser dateParamValueParser;

    @Test
    public void testIsInscriptionEnabledIsTrue() {
        // GIVEN
        ParamEntity param = new ParamEntity();
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ENABLED_FROM_DATE))).thenReturn(param);
        when(this.dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.parse("01.01.1950", DateTimeFormatter.ofPattern(APIDateFormats.DATE_FORMAT)));

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionEnabled();

        // THEN
        assertTrue(isInscriptionEnabled);
    }

    @Test
    public void testIsInscriptionEnabledIsFalse() {
        // GIVEN
        ParamEntity param = new ParamEntity();
        when(this.paramRepository.findByName(Mockito.eq(ParamNameEnum.INSCRIPTION_ENABLED_FROM_DATE))).thenReturn(param);
        when(this.dateParamValueParser.getValue(Mockito.any())).thenReturn(LocalDate.now().plusDays(1));

        // WHEN
        boolean isInscriptionEnabled = underTest.isInscriptionEnabled();

        // THEN
        assertFalse(isInscriptionEnabled);
    }
}
