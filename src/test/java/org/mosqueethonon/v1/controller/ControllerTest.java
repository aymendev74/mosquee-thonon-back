package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
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

    protected String generateToken(String role) {
        UtilisateurEntity user = new UtilisateurEntity();
        user.setId(1L);
        user.setUsername("aymen");
        if(role != null) {
            UtilisateurRoleEntity roleUtilisateur = new UtilisateurRoleEntity();
            roleUtilisateur.setRole(role);
            user.setRoles(List.of(roleUtilisateur));
        }
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
                .type(TypeTarifEnum.BASE.name()).code("BASE_ADHERENT_1_ENFANT").montant(bd(120)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE.name()).code("BASE_1_ENFANT").montant(bd(240)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE.name()).code("BASE_ADHERENT_2_ENFANT").montant(bd(160)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE.name()).code("BASE_2_ENFANT").montant(bd(280)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE.name()).code("BASE_ADHERENT_3_ENFANT").montant(bd(200)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE.name()).code("BASE_3_ENFANT").montant(bd(320)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE.name()).code("BASE_ADHERENT_4_ENFANT").montant(bd(240)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE.name()).code("BASE_4_ENFANT").montant(bd(360)).build());

        // tarifs par enfant
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_ADHERENT_1_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_1_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_ADHERENT_2_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_2_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_ADHERENT_3_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_3_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_ADHERENT_4_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT.name()).code("ENFANT_4_ENFANT").montant(bd(15)).build());

        // tarifs adultes
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.ADULTE.name()).montant(bd(15)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    private void initTarifsCoursAdulte(PeriodeEntity periode) {
        List<TarifEntity> tarifsCours = new ArrayList<>();

        // tarifs adultes
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.ADULTE.name()).montant(bd(15)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    private PeriodeEntity initPeriodeCoursEnfant() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(ApplicationTarifEnum.COURS_ENFANT.name()).dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).nbMaxInscription(500).build();
        return this.periodeRepository.save(periode);
    }

    private PeriodeEntity initPeriodeCoursAdulte() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(ApplicationTarifEnum.COURS_ADULTE.name()).dateDebut(today.minusDays(1))
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
