package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionLightEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionLightRepository;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionAdulteDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestInscriptionAdulteController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    protected InscriptionAdulteRepository inscriptionAdulteRepository;

    @Autowired
    protected InscriptionLightRepository inscriptionLightRepository;

    @Autowired
    protected DocumentRequestRepository documentRequestRepository;

    @Autowired
    private ParamRepository paramRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    protected void initReferentiel() {
        this.initParams();
        this.initMatieres();
        this.initTarifsCoursAdulte(this.initPeriodeCoursAdulte());
    }

    private void initMatieres() {
        jdbcTemplate.execute("INSERT INTO moth.ref_matiere (idmati, cdmaticode, cdmatitype) VALUES (100, 'TAFFSIR_CORAN', 'ADULTE')");
    }

    private void initParams() {
        ParamEntity paramInscriptionEnabled = new ParamEntity();
        paramInscriptionEnabled.setName(ParamNameEnum.INSCRIPTION_ADULTE_ENABLED_FROM_DATE);
        paramInscriptionEnabled.setValue("01.01.1950");
        this.paramRepository.save(paramInscriptionEnabled);
    }

    private void initTarifsCoursAdulte(PeriodeEntity periode) {
        List<TarifEntity> tarifsCours = new ArrayList<>();

        // tarifs adultes
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.ETUDIANT).montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.SANS_ACTIVITE).montant(bd(20)).build());
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.AVEC_ACTIVITE).montant(bd(40)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    private PeriodeEntity initPeriodeCoursAdulte() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(ApplicationTarifEnum.COURS_ADULTE.name()).dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).build();
        return this.periodeRepository.save(periode);
    }

    @BeforeEach
    protected void deleteInscription() {
        this.inscriptionAdulteRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testSaveInscriptionAdulteMultiThreading() throws Exception {
        int nbThreads = 50;
        CountDownLatch compteur = new CountDownLatch(nbThreads);

        for (int i = 0; i < nbThreads; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte()))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                            .andExpect(MockMvcResultMatchers.status().isOk());
                } catch (Exception e) {
                    fail(e);
                } finally {
                    compteur.countDown();
                }

            }).start();
        }

        // On attend que tous les threads aient terminé leur inscription
        compteur.await();

        // Puis on va vérifier que 510 inscriptions ont bien été enregistrées
        List<InscriptionAdulteEntity> allInscriptions = this.inscriptionAdulteRepository.findAll();
        // Vérification d'un certain nombre de critères (ajustez selon vos besoins)
        Long nbInscriptionsValides = allInscriptions.stream()
                .filter(inscription -> inscription.getStatut() == StatutInscription.PROVISOIRE)
                .count();
        assertEquals(50, nbInscriptionsValides);
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testReinscription_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes/reinscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createReinscriptionAdulte()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        List<InscriptionAdulteEntity> allInscriptions = this.inscriptionAdulteRepository.findAll();
        assertEquals(1, allInscriptions.size());
        assertEquals(StatutInscription.VALIDEE, allInscriptions.get(0).getStatut());
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testReinscription_InscriptionClosed() throws Exception {
        // On met une date dans le futur pour fermer les inscriptions
        ParamEntity param = this.paramRepository.findByName(ParamNameEnum.INSCRIPTION_ADULTE_ENABLED_FROM_DATE);
        param.setValue("01.01.2099");
        this.paramRepository.save(param);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes/reinscription")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(this.createReinscriptionAdulte()))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(MockMvcResultMatchers.status().isInternalServerError());

            List<InscriptionAdulteEntity> allInscriptions = this.inscriptionAdulteRepository.findAll();
            assertEquals(0, allInscriptions.size());
        } finally {
            // On remet la valeur d'origine pour ne pas impacter les autres tests
            param.setValue("01.01.1950");
            this.paramRepository.save(param);
        }
    }

    private InscriptionAdulteDto createInscriptionAdulte() {
        InscriptionAdulteDto inscriptionDto = new InscriptionAdulteDto();
        inscriptionDto.setNom("Doe");
        inscriptionDto.setPrenom("John");
        inscriptionDto.setEmail("john.doe@example.com");
        inscriptionDto.setMobile("0600000000");
        inscriptionDto.setNumeroEtRue("10bis, rue de la mosquee");
        inscriptionDto.setCodePostal(74200);
        inscriptionDto.setVille("Thonon");
        inscriptionDto.setDateNaissance(LocalDate.of(1990, 1, 1));
        inscriptionDto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        return inscriptionDto;
    }

    private ReinscriptionAdulteDto createReinscriptionAdulte() {
        ReinscriptionAdulteDto dto = new ReinscriptionAdulteDto();
        dto.setNom("Dupont");
        dto.setPrenom("Jean");
        dto.setEmail("jean.dupont@example.com");
        dto.setMobile("0612345678");
        dto.setNumeroEtRue("10 rue de la paix");
        dto.setCodePostal(74200);
        dto.setVille("Thonon");
        dto.setDateNaissance(LocalDate.of(1990, 5, 15));
        dto.setSexe(SexeEnum.M);
        dto.setNiveauInterne(NiveauInterneEnum.DEBUTANT);
        dto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        dto.setMatieres(List.of(MatiereEnum.TAFFSIR_CORAN));
        return dto;
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testInscriptionAdulteLight_DocumentPendingTrueAfterCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Long idInscription = this.inscriptionAdulteRepository.findAll().get(0).getId();
        InscriptionLightEntity light = inscriptionLightRepository.findAll().stream()
                .filter(l -> idInscription.equals(l.getIdInscription()))
                .findFirst().orElseThrow();
        assertTrue(light.getDocumentPending());
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testInscriptionAdulteLight_DocumentPendingFalseWhenRequestCompleted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Long idInscription = this.inscriptionAdulteRepository.findAll().get(0).getId();
        DocumentRequestEntity request = documentRequestRepository
                .findByTypeAndBusinessIdAndStatut(DocumentRequestType.INSCRIPTION_ADULTE, idInscription, DocumentRequestStatut.PENDING)
                .orElseThrow();
        request.setStatut(DocumentRequestStatut.COMPLETED);
        documentRequestRepository.save(request);

        InscriptionLightEntity light = inscriptionLightRepository.findAll().stream()
                .filter(l -> idInscription.equals(l.getIdInscription()))
                .findFirst().orElseThrow();
        assertFalse(light.getDocumentPending());
    }

}
