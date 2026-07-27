package org.mosqueethonon.utilisateur.scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.security.ApplicationConfiguration;
import org.mosqueethonon.mail.enums.MailRequestStatutEnum;
import org.mosqueethonon.param.service.ParamService;
import org.mosqueethonon.referentiel.service.TraductionService;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;
import org.mosqueethonon.utilisateur.entity.UserAccountActionEntity;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;
import org.mosqueethonon.utilisateur.enums.UserAccountActionTypeEnum;
import org.mosqueethonon.utilisateur.repository.UserAccountActionRepository;
import org.mosqueethonon.utilisateur.repository.UtilisateurRepository;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
public class TestMailActivationUtilisateurJob {

    @Mock
    private ParamService paramService;

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private TraductionService traductionService;

    @Mock
    private UserAccountActionRepository userAccountActionRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @InjectMocks
    private MailActivationUtilisateurJob underTest;

    private UserAccountActionEntity action;

    @BeforeEach
    public void setUp() {
        this.action = new UserAccountActionEntity();
        this.action.setUsername("jdupont");
        this.action.setToken("TOK123");
        this.action.setStatut(MailRequestStatutEnum.PENDING);
        this.action.setType(UserAccountActionTypeEnum.ACTIVATION);
    }

    private void givenActionsEnAttente(UserAccountActionEntity... actions) {
        when(this.userAccountActionRepository.findByStatutAndTypeOrderBySignatureDateCreationAsc(
                MailRequestStatutEnum.PENDING, UserAccountActionTypeEnum.ACTIVATION))
                .thenReturn(List.of(actions));
    }

    private TraductionDto traduction(String fr) {
        return TraductionDto.builder().fr(fr).build();
    }

    private UtilisateurEntity utilisateur(String prenom) {
        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setUsername("jdupont");
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail("jean@example.org");
        return utilisateur;
    }

    /** Un vrai MimeMessage : MimeMessageHelper écrit dedans, un mock ne suffirait pas. */
    private MimeMessage mimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    /** MimeMessageHelper construit un message multipart : le corps HTML est dans une sous-partie. */
    private String corpsDe(MimeMessage message) throws Exception {
        Object contenu = message.getContent();
        if (contenu instanceof jakarta.mail.Multipart multipart) {
            StringBuilder texte = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                Object partie = multipart.getBodyPart(i).getContent();
                if (partie instanceof jakarta.mail.Multipart imbrique) {
                    for (int j = 0; j < imbrique.getCount(); j++) {
                        texte.append(imbrique.getBodyPart(j).getContent());
                    }
                } else {
                    texte.append(partie);
                }
            }
            return texte.toString();
        }
        return String.valueOf(contenu);
    }

    private void givenEnvoiPossible(UtilisateurEntity utilisateur, String corps) {
        when(this.paramService.isSendEmailEnabled()).thenReturn(true);
        when(this.utilisateurRepository.findByUsername("jdupont")).thenReturn(Optional.of(utilisateur));
        when(this.traductionService.findTraductionByCleAndValeur("mail_activation", "subject"))
                .thenReturn(traduction("Activation de votre compte"));
        when(this.traductionService.findTraductionByCleAndValeur("mail_activation", "body"))
                .thenReturn(traduction(corps));
        when(this.applicationConfiguration.getActivationUtilisateurUri())
                .thenReturn("https://mosquee.test/activation");
        when(this.emailSender.createMimeMessage()).thenReturn(mimeMessage());
    }

    @Test
    public void testNeFaitRienSansDemandeEnAttente() {
        // GIVEN
        when(userAccountActionRepository.findByStatutAndTypeOrderBySignatureDateCreationAsc(
                MailRequestStatutEnum.PENDING, UserAccountActionTypeEnum.ACTIVATION))
                .thenReturn(Collections.emptyList());

        // WHEN
        underTest.sendPendingEmailsActivation();

        // THEN
        Mockito.verifyNoInteractions(emailSender, paramService, traductionService);
        verify(userAccountActionRepository, never()).save(any());
    }

    @Test
    public void testIgnoreLesDemandesQuandLEnvoiDeMailEstDesactive() {
        // GIVEN
        givenActionsEnAttente(action);
        when(paramService.isSendEmailEnabled()).thenReturn(false);

        // WHEN
        underTest.sendPendingEmailsActivation();

        // THEN — la demande est marquée IGNORED, pas ERROR : ce n'est pas un échec
        assertEquals(MailRequestStatutEnum.IGNORED, action.getStatut());
        verify(userAccountActionRepository).save(action);
        Mockito.verifyNoInteractions(emailSender);
    }

    @Test
    public void testEnvoieLeMailEtMarqueLaDemandeEnvoyee() throws Exception {
        // GIVEN
        givenActionsEnAttente(action);
        givenEnvoiPossible(utilisateur("Jean"), "Bonjour @@{username}, activez ici : @@{activationUrl}");

        // WHEN
        underTest.sendPendingEmailsActivation();

        // THEN
        assertEquals(MailRequestStatutEnum.SENT, action.getStatut());
        verify(userAccountActionRepository).save(action);
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(emailSender).send(captor.capture());
        MimeMessage envoye = captor.getValue();
        assertEquals("Activation de votre compte", envoye.getSubject());
        assertEquals("jean@example.org", envoye.getAllRecipients()[0].toString());
        String corps = corpsDe(envoye);
        assertTrue(corps.contains("Bonjour Jean"), corps);
        assertTrue(corps.contains("https://mosquee.test/activation?token=TOK123"), corps);
    }

    @Test
    public void testUtiliseLeLoginQuandLePrenomEstAbsent() throws Exception {
        // GIVEN
        givenActionsEnAttente(action);
        givenEnvoiPossible(utilisateur(null), "Bonjour @@{username} @@{activationUrl}");

        // WHEN
        underTest.sendPendingEmailsActivation();

        // THEN
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(emailSender).send(captor.capture());
        assertTrue(corpsDe(captor.getValue()).contains("Bonjour jdupont"));
    }

    @Test
    public void testMarqueEnErreurQuandLUtilisateurNexistePas() {
        // GIVEN
        givenActionsEnAttente(action);
        when(paramService.isSendEmailEnabled()).thenReturn(true);
        when(utilisateurRepository.findByUsername("jdupont")).thenReturn(Optional.empty());

        // WHEN — l'exception ne doit pas interrompre le job
        underTest.sendPendingEmailsActivation();

        // THEN
        assertEquals(MailRequestStatutEnum.ERROR, action.getStatut());
        verify(userAccountActionRepository).save(action);
    }

    @Test
    public void testMarqueEnErreurQuandLEnvoiEchoue() {
        // GIVEN
        givenActionsEnAttente(action);
        givenEnvoiPossible(utilisateur("Jean"), "Bonjour @@{username} @@{activationUrl}");
        Mockito.doThrow(new org.springframework.mail.MailSendException("SMTP indisponible"))
                .when(emailSender).send(any(MimeMessage.class));

        // WHEN
        underTest.sendPendingEmailsActivation();

        // THEN
        assertEquals(MailRequestStatutEnum.ERROR, action.getStatut());
        verify(userAccountActionRepository).save(action);
    }

    @Test
    public void testUnEchecNInterrompsPasLesAutresDemandes() {
        // GIVEN — la première demande échoue, la seconde doit quand même être traitée
        UserAccountActionEntity enEchec = new UserAccountActionEntity();
        enEchec.setUsername("inconnu");
        enEchec.setStatut(MailRequestStatutEnum.PENDING);
        givenActionsEnAttente(enEchec, action);
        when(paramService.isSendEmailEnabled()).thenReturn(false);

        // WHEN
        underTest.sendPendingEmailsActivation();

        // THEN
        assertEquals(MailRequestStatutEnum.IGNORED, enEchec.getStatut());
        assertEquals(MailRequestStatutEnum.IGNORED, action.getStatut());
        verify(userAccountActionRepository, Mockito.times(2)).save(any(UserAccountActionEntity.class));
    }
}
