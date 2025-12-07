package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAdhesionController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AdhesionDto testAdhesion;

    @Autowired
    protected AdhesionRepository adhesionRepository;

    @BeforeAll
    public void init() {
        this.initTarifAdhesion(this.initPeriodeAdhesion());
    }

    @BeforeEach
    public void setup() {
        testAdhesion = new AdhesionDto();
        TarifEntity tarifAdhesion = this.findFirstTarifAdhesion();
        testAdhesion.setIdTarif(tarifAdhesion.getId());
    }

    private void initTarifAdhesion(PeriodeEntity periode) {
        // tarif adhésion
        TarifEntity tarifAdhesion = TarifEntity.builder().periode(periode).type(TypeTarifEnum.FIXE).montant(bd(15)).build();

        this.tarifRepository.save(tarifAdhesion);
    }

    private PeriodeEntity initPeriodeAdhesion() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(ApplicationTarifEnum.ADHESION.name()).dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).build();
        return this.periodeRepository.save(periode);
    }


    private TarifEntity findFirstTarifAdhesion() {
        return this.tarifRepository.findAll().stream().filter(tarif -> tarif.getPeriode().getApplication().equals("ADHESION"))
                .findFirst().orElseThrow(() -> new IllegalStateException("Aucun tarif de type adhésion trouvé !"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCreateAdhesion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/adhesions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAdhesion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.statut").value(StatutInscription.PROVISOIRE.name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAdhesionById() throws Exception {
        // Création d'une adhésion pour le test
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/v1/adhesions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdhesion)))
                .andReturn().getResponse().getContentAsString();
        
        AdhesionDto createdAdhesion = objectMapper.readValue(response, AdhesionDto.class);
        
        // Test de récupération
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/adhesions/" + createdAdhesion.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAdhesion.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateAdhesion() throws Exception {
        // Création d'une adhésion pour le test
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/v1/adhesions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdhesion)))
                .andReturn().getResponse().getContentAsString();
        
        AdhesionDto createdAdhesion = objectMapper.readValue(response, AdhesionDto.class);
        
        // Mise à jour de l'adhésion
        createdAdhesion.setMontant(new BigDecimal("75.0"));
        createdAdhesion.setStatut(StatutInscription.VALIDEE);
        
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/adhesions/" + createdAdhesion.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdAdhesion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montant").value(75.0))
                .andExpect(jsonPath("$.statut").value(StatutInscription.VALIDEE.name()));
    }

    @Test
    @WithMockUser(roles = "ENSEIGNANT")
    public void testUpdateAdhesionWithNoAuthorizedRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/adhesions/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdhesion)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRESORIER")
    public void testFindAdhesionsByCriteriaWithTresorierRole() throws Exception {
        // Recherche avec critères
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/adhesions")
                .param("statut", StatutInscription.PROVISOIRE.name()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testFindAdhesionsByCriteriaWithAdminRole() throws Exception {
        // Recherche avec critères
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/adhesions")
                        .param("statut", StatutInscription.PROVISOIRE.name()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ENSEIGNANT")
    public void testFindAdhesionsByCriteriaWithNoAuthorizedRole() throws Exception {
        // Recherche avec critères
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/adhesions")
                        .param("statut", StatutInscription.PROVISOIRE.name()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteAdhesions() throws Exception {
        // Création d'une adhésion pour le test
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/v1/adhesions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdhesion)))
                .andReturn().getResponse().getContentAsString();
        
        AdhesionDto createdAdhesion = objectMapper.readValue(response, AdhesionDto.class);
        
        // Suppression de l'adhésion
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/adhesions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + createdAdhesion.getId() + "]"))
                .andExpect(status().isOk());
        
        // Vérification que l'adhésion a bien été supprimée
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/adhesions/" + createdAdhesion.getId()))
                .andExpect(status().isNotFound());
    }
}
