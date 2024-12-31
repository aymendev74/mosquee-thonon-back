package org.mosqueethonon.v1.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.enums.ResultatEnum;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.mapper.inscription.*;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class TestInscriptionEnfantMapperImpl {

    @Mock
    private ResponsableLegalMapper responsableLegalMapper;

    @Spy
    private EleveMapper eleveMapper = new EleveMapperImpl();

    @InjectMocks
    private InscriptionEnfantMapperImpl inscriptionEnfantMapper;

    @BeforeEach
    public void init() {
        inscriptionEnfantMapper.setEleveMapper(eleveMapper);
    }

    @Test
    public void testUpdateInscriptionEntity() {
        // GIVEN
        InscriptionEnfantDto inscriptionEnfantDto = new InscriptionEnfantDto();
        EleveDto eleveDto = new EleveDto();
        eleveDto.setId(1L);
        eleveDto.setNom("nomUpdated");
        eleveDto.setPrenom("prenomUpdated");
        eleveDto.setDateNaissance(LocalDate.of(2000, 1, 1));
        eleveDto.setNiveau(NiveauScolaireEnum.CE1);
        eleveDto.setNiveauInterne(NiveauInterneEnum.P1);
        inscriptionEnfantDto.setEleves(Lists.newArrayList(eleveDto));

        InscriptionEnfantEntity inscriptionEnfantEntity = new InscriptionEnfantEntity();
        EleveEntity eleveEntity = new EleveEntity();
        eleveEntity.setId(1L);
        eleveEntity.setNom("nom");
        eleveEntity.setPrenom("prenom");
        eleveEntity.setDateNaissance(LocalDate.of(2001, 1, 1));
        eleveEntity.setNiveau(NiveauScolaireEnum.CP);
        eleveEntity.setNiveauInterne(NiveauInterneEnum.P2);
        eleveEntity.setResultat(ResultatEnum.ACQUIS);
        inscriptionEnfantEntity.setEleves(Lists.newArrayList(eleveEntity));

        // WHEN
        inscriptionEnfantMapper.updateInscriptionEntity(inscriptionEnfantDto, inscriptionEnfantEntity);

        // THEN
        Mockito.verify(responsableLegalMapper).fromDtoToEntity(inscriptionEnfantDto.getResponsableLegal());
        Mockito.verify(eleveMapper).updateEleves(Mockito.eq(inscriptionEnfantDto.getEleves()), Mockito.eq(inscriptionEnfantEntity.getEleves()));
        EleveEntity eleveEntityUpdated = inscriptionEnfantEntity.getEleves().get(0);
        assertEquals(1L, eleveEntityUpdated.getId());
        assertEquals("nomUpdated", eleveEntityUpdated.getNom());
        assertEquals("prenomUpdated", eleveEntityUpdated.getPrenom());
        assertEquals(LocalDate.of(2000, 1, 1), eleveEntityUpdated.getDateNaissance());
        assertEquals(NiveauScolaireEnum.CE1, eleveEntityUpdated.getNiveau());
        assertEquals(NiveauInterneEnum.P1, eleveEntityUpdated.getNiveauInterne());
        assertEquals(ResultatEnum.ACQUIS, eleveEntityUpdated.getResultat());
    }

}
