package org.mosqueethonon.v1.dto.bulletin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NoteMatiereEnum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestBulletinDto {

    private BulletinDto bulletinComplet() {
        BulletinDto dto = new BulletinDto();
        dto.setAppreciation("Bon élève");
        dto.setNbAbsences(2);
        dto.setMois(3);
        dto.setAnnee(2025);
        dto.setDateBulletin(LocalDate.of(2025, 3, 31));
        BulletinMatiereDto bm = BulletinMatiereDto.builder()
                .code(MatiereEnum.CORAN)
                .note(NoteMatiereEnum.A)
                .build();
        dto.setBulletinMatieres(new ArrayList<>(List.of(bm)));
        return dto;
    }

    @Test
    public void testCalculerCompletude_RetourneTrue_QuandTousLesChampsRenseignes() {
        BulletinDto dto = bulletinComplet();
        assertTrue(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandAppreciationNulle() {
        BulletinDto dto = bulletinComplet();
        dto.setAppreciation(null);
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandAppreciationBlanche() {
        BulletinDto dto = bulletinComplet();
        dto.setAppreciation("   ");
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandNbAbsencesNull() {
        BulletinDto dto = bulletinComplet();
        dto.setNbAbsences(null);
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandMoisNull() {
        BulletinDto dto = bulletinComplet();
        dto.setMois(null);
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandAnneeNull() {
        BulletinDto dto = bulletinComplet();
        dto.setAnnee(null);
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandDateBulletinNull() {
        BulletinDto dto = bulletinComplet();
        dto.setDateBulletin(null);
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandAucuneMatiereRequise() {
        BulletinDto dto = bulletinComplet();
        assertFalse(dto.calculerCompletude(List.of()));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandNoteMatiereManquante() {
        BulletinDto dto = bulletinComplet();
        dto.getBulletinMatieres().get(0).setNote(null);
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN)));
    }

    @Test
    public void testCalculerCompletude_RetourneFalse_QuandMatiereRequiseAbsenteDuBulletin() {
        BulletinDto dto = bulletinComplet();
        assertFalse(dto.calculerCompletude(List.of(MatiereEnum.CORAN, MatiereEnum.DICTEE)));
    }
}
