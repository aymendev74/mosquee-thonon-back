package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.BeforeAll;
import org.mosqueethonon.authentication.jwt.JwtTokenUtil;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.entity.UtilisateurEntity;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.TarifRepository;
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
    protected InscriptionRepository inscriptionRepository;

    protected String generateToken() {
        UtilisateurEntity user = new UtilisateurEntity();
        user.setId(1L);
        user.setUsername("aymen");
        return new StringBuilder("Bearer ").append(tokenUtil.generateAccessToken(user)).toString();
    }

    @BeforeAll
    protected void initReferentiel() {
        PeriodeEntity periodeCours = this.initPeriode(ApplicationTarifEnum.COURS.name());
        PeriodeEntity periodeAdhesion = this.initPeriode(ApplicationTarifEnum.ADHESION.name());
        this.initTarifs(periodeCours, periodeAdhesion);
    }

    private void initTarifs(PeriodeEntity... periodes) {
        for(PeriodeEntity periode : periodes) {
            if(periode.getApplication().equals(ApplicationTarifEnum.COURS.name())) {
                this.initTarifsCours(periode);
            } else {
                this.initTarifsAdhesion(periode);
            }
        }
    }

    private void initTarifsAdhesion(PeriodeEntity periode) {
        List<TarifEntity> tarifsAdhesion = new ArrayList<>();
        // tarifs base
        tarifsAdhesion.add(TarifEntity.builder().periode(periode)
                .type("FIXE").montant(bd(15)).build());
        tarifsAdhesion.add(TarifEntity.builder().periode(periode)
                .type("FIXE").montant(bd(20)).build());
        tarifsAdhesion.add(TarifEntity.builder().periode(periode)
                .type("FIXE").montant(bd(30)).build());
        tarifsAdhesion.add(TarifEntity.builder().periode(periode)
                .type("LIBRE").build());
        this.tarifRepository.saveAll(tarifsAdhesion);
    }

    private void initTarifsCours(PeriodeEntity periode) {
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

        this.tarifRepository.saveAll(tarifsCours);
    }

    private PeriodeEntity initPeriode(String application) {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(application).dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).nbMaxInscription("COURS".equals(application) ? 500 : null).build();
        return this.periodeRepository.save(periode);
    }

    public static final BigDecimal bd(Integer bdAsInt) {
        return BigDecimal.valueOf(bdAsInt);
    }
}
