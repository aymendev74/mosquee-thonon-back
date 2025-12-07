package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.TestInstance;
import org.mosqueethonon.Application;
import org.mosqueethonon.configuration.exception.CustomExceptionHandler;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.service.inscription.InscriptionOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.math.BigDecimal;

@SpringBootTest(classes = { Application.class, CustomExceptionHandler.class })
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(scripts = "/after-init.sql",
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class TestController {

    @Autowired
    protected PeriodeRepository periodeRepository;

    @Autowired
    protected TarifRepository tarifRepository;

    @Autowired
    protected InscriptionOrchestratorService inscriptionOrchestratorService;

    public static BigDecimal bd(Integer bdAsInt) {
        return BigDecimal.valueOf(bdAsInt);
    }
}
