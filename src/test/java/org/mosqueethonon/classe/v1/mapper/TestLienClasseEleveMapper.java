package org.mosqueethonon.classe.v1.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.classe.entity.LienClasseEleveEntity;
import org.mosqueethonon.classe.v1.dto.LienClasseEleveDto;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.inscription.repository.EleveRepository;
import org.mosqueethonon.inscription.v1.dto.EleveDto;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestLienClasseEleveMapper {

    @Mock
    private EleveRepository eleveRepository;

    @Mock
    private org.mosqueethonon.inscription.v1.mapper.EleveMapper eleveMapper;

    private LienClasseEleveMapper underTest;

    @BeforeEach
    public void setUp() {
        underTest = new LienClasseEleveMapperImpl();
        ReflectionTestUtils.setField(underTest, "eleveRepository", eleveRepository);
    }

    private LienClasseEleveDto lienAvecEleve(Long idEleve) {
        LienClasseEleveDto dto = new LienClasseEleveDto();
        dto.setEleve(EleveDto.builder().id(idEleve).build());
        return dto;
    }

    @Test
    public void testDtoNull() {
        assertNull(underTest.fromDtoToEntity(null));
    }

    @Test
    public void testRechargeLElevePourRecupererSaVersionEtSesChamps() {
        EleveEntity eleve = new EleveEntity();
        eleve.setId(3L);
        when(eleveRepository.findById(3L)).thenReturn(Optional.of(eleve));

        LienClasseEleveEntity entity = underTest.fromDtoToEntity(lienAvecEleve(3L));

        assertSame(eleve, entity.getEleve());
    }

    @Test
    public void testEleveIntrouvable() {
        when(eleveRepository.findById(404L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> underTest.fromDtoToEntity(lienAvecEleve(404L)));

        assertTrue(exception.getMessage().contains("404"));
    }

    @Test
    public void testAucunEleveDansLeLien() {
        LienClasseEleveEntity entity = underTest.fromDtoToEntity(new LienClasseEleveDto());

        assertNull(entity.getEleve());
        verify(eleveRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    public void testEleveSansIdentifiantNestPasRecharge() {
        LienClasseEleveEntity entity = underTest.fromDtoToEntity(lienAvecEleve(null));

        assertNull(entity.getEleve());
        verify(eleveRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    public void testEntityNull() {
        assertNull(underTest.fromEntityToDto(null));
    }

    @Test
    public void testMappeLEleveVersLeDto() {
        ReflectionTestUtils.setField(underTest, "eleveMapper", eleveMapper);

        EleveEntity eleve = new EleveEntity();
        eleve.setId(3L);
        when(eleveMapper.fromEntityToDto(eleve)).thenReturn(EleveDto.builder().id(3L).build());

        LienClasseEleveDto dto = underTest.fromEntityToDto(
                LienClasseEleveEntity.builder().eleve(eleve).build());

        assertEquals(3L, dto.getEleve().getId());
    }
}
