package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mosqueethonon.authentication.jwt.JwtTokenUtil;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.entity.UtilisateurEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ControllerTest {


    @Autowired
    private JwtTokenUtil tokenUtil;

    @Autowired
    protected PeriodeRepository periodeRepository;

    @Autowired
    protected TarifRepository tarifRepository;

    @Autowired
    protected InscriptionEnfantRepository inscriptionEnfantRepository;

    @Autowired
    protected InscriptionAdulteRepository inscriptionAdulteRepository;

    @Autowired
    private ParamRepository paramRepository;

    protected String generateToken() {
        UtilisateurEntity user = new UtilisateurEntity();
        user.setId(1L);
        user.setUsername("aymen");
        return new StringBuilder("Bearer ").append(tokenUtil.generateAccessToken(user)).toString();
    }

    @BeforeAll
    protected void initReferentiel() {
        this.initParams();
        this.initTarifsCoursEnfant(this.initPeriodeCoursEnfant());
        this.initTarifsCoursAdulte(this.initPeriodeCoursAdulte());
    }

    @BeforeEach
    protected void deleteInscription() {
        this.inscriptionEnfantRepository.deleteAll();
    }

    private void initTarifsCoursEnfant(PeriodeEntity periode) {
        List<TarifEntity> tarifsCours = new ArrayList<>();
        // tarifs base
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(true)
                .type("BASE").code("BASE_ADHERENT_1_ENFANT").montant(bd(120)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(false)
                .type("BASE").code("BASE_1_ENFANT").montant(bd(240)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(true)
                .type("BASE").code("BASE_ADHERENT_2_ENFANT").montant(bd(160)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(false)
                .type("BASE").code("BASE_2_ENFANT").montant(bd(280)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(true)
                .type("BASE").code("BASE_ADHERENT_3_ENFANT").montant(bd(200)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(false)
                .type("BASE").code("BASE_3_ENFANT").montant(bd(320)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(true)
                .type("BASE").code("BASE_ADHERENT_4_ENFANT").montant(bd(240)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(false)
                .type("BASE").code("BASE_4_ENFANT").montant(bd(360)).build());

        // tarifs par enfant
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(true)
                .type("ENFANT").code("ENFANT_ADHERENT_1_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(false)
                .type("ENFANT").code("ENFANT_1_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(true)
                .type("ENFANT").code("ENFANT_ADHERENT_2_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(false)
                .type("ENFANT").code("ENFANT_2_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(true)
                .type("ENFANT").code("ENFANT_ADHERENT_3_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(false)
                .type("ENFANT").code("ENFANT_3_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(true)
                .type("ENFANT").code("ENFANT_ADHERENT_4_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(false)
                .type("ENFANT").code("ENFANT_4_ENFANT").montant(bd(15)).build());

        // tarifs adultes
        tarifsCours.add(TarifEntity.builder().periode(periode).type("COURS").montant(bd(15)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    private void initTarifsCoursAdulte(PeriodeEntity periode) {
        List<TarifEntity> tarifsCours = new ArrayList<>();

        // tarifs adultes
        tarifsCours.add(TarifEntity.builder().periode(periode).type("ADULTE").montant(bd(15)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    private PeriodeEntity initPeriodeCoursEnfant() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application("COURS").dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).nbMaxInscription(500).build();
        return this.periodeRepository.save(periode);
    }

    private PeriodeEntity initPeriodeCoursAdulte() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application("COURS_ADULTE").dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).build();
        return this.periodeRepository.save(periode);
    }

    private void initParams() {
        ParamEntity paramInscriptionEnabled = new ParamEntity();
        paramInscriptionEnabled.setName(ParamNameEnum.INSCRIPTION_ENABLED_FROM_DATE);
        paramInscriptionEnabled.setValue("01.01.1950");
        this.paramRepository.save(paramInscriptionEnabled);
    }

    public static final BigDecimal bd(Integer bdAsInt) {
        return BigDecimal.valueOf(bdAsInt);
    }
}
