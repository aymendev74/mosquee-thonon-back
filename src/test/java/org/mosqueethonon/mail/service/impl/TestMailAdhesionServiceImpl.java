package org.mosqueethonon.mail.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.adhesion.service.AdhesionService;
import org.mosqueethonon.adhesion.v1.dto.AdhesionDto;
import org.mosqueethonon.common.security.ApplicationConfiguration;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;
import org.mosqueethonon.mail.dto.MailDto;
import org.mosqueethonon.referentiel.service.TraductionService;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;

@ExtendWith(MockitoExtension.class)
public class TestMailAdhesionServiceImpl {

    @Mock
    private TraductionService traductionService;

    @Mock
    private AdhesionService adhesionService;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @InjectMocks
    private MailAdhesionServiceImpl underTest;

    private AdhesionDto adhesion(StatutInscriptionEnum statut) {
        AdhesionDto adhesion = new AdhesionDto();
        adhesion.setNom("Dupont");
        adhesion.setPrenom("Jean");
        adhesion.setEmail("jean@example.org");
        adhesion.setStatut(statut);
        return adhesion;
    }

    private void givenTraductions(String statut, String corps) {
        when(this.traductionService.findTraductionByCleAndValeur("mail_adhesion", "subject"))
                .thenReturn(TraductionDto.builder().fr("Votre adhésion").build());
        when(this.traductionService.findTraductionByCleAndValeur("mail_adhesion_" + statut, "body"))
                .thenReturn(TraductionDto.builder().fr(corps).build());
    }

    @Test
    public void testRetourneNullQuandLAdhesionNexistePas() {
        // GIVEN
        when(adhesionService.findAdhesionById(404L)).thenReturn(null);

        // WHEN
        MailDto result = underTest.createMail(404L);

        // THEN
        assertNull(result);
        Mockito.verifyNoInteractions(traductionService, applicationConfiguration);
    }

    @Test
    public void testChoisitLeTemplateSelonLeStatutEtRemplaceLesPlaceholders() {
        // GIVEN
        when(adhesionService.findAdhesionById(1L)).thenReturn(adhesion(StatutInscriptionEnum.PROVISOIRE));
        givenTraductions("provisoire", "Bonjour @@{prenom} @@{nom}, votre demande est reçue.");

        // WHEN
        MailDto result = underTest.createMail(1L);

        // THEN
        assertEquals("Votre adhésion", result.subject());
        assertEquals("jean@example.org", result.recipientEmail());
        assertEquals("Bonjour Jean Dupont, votre demande est reçue.", result.body());
    }

    @Test
    public void testNAttachePasLeRibTantQueLAdhesionNestPasValidee() {
        // GIVEN
        when(adhesionService.findAdhesionById(1L)).thenReturn(adhesion(StatutInscriptionEnum.PROVISOIRE));
        givenTraductions("provisoire", "corps");

        // WHEN
        MailDto result = underTest.createMail(1L);

        // THEN
        assertTrue(result.attachments().isEmpty());
        Mockito.verifyNoInteractions(applicationConfiguration);
    }

    @Test
    public void testAttacheLeRibQuandLAdhesionEstValidee() {
        // GIVEN
        ApplicationConfiguration.RibAmc ribAmc = new ApplicationConfiguration.RibAmc();
        ribAmc.setFileLocation("/documents/rib.pdf");
        ribAmc.setMailAttachmentFilename("RIB-AMC.pdf");
        when(adhesionService.findAdhesionById(1L)).thenReturn(adhesion(StatutInscriptionEnum.VALIDEE));
        givenTraductions("validee", "corps");
        when(applicationConfiguration.getRibAmc()).thenReturn(ribAmc);

        // WHEN
        MailDto result = underTest.createMail(1L);

        // THEN — le RIB n'est joint qu'une fois l'adhésion validée
        assertEquals(1, result.attachments().size());
        assertEquals("RIB-AMC.pdf", result.attachments().get(0).getName());
        assertEquals("/documents/rib.pdf", result.attachments().get(0).getLocation());
    }
}
