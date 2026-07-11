package org.mosqueethonon.service.impl.inscription;

import org.mosqueethonon.configuration.security.context.Roles;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.mail.MailRequestDocumentRequestEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.impl.UserAccountManager;
import org.mosqueethonon.v1.dto.user.RoleDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;

public class CommonInscriptionService {

    @Autowired
    protected UserAccountManager userAccountManager;

    @Autowired
    protected InscriptionRepository inscriptionRepository;

    @Autowired
    protected MailRequestRepository mailRequestRepository;

    protected UserAccountResult manageUserAccount(String email, String nom, String prenom, String mobile) {
        Optional<UserDto> existingUser = this.userAccountManager.findByEmail(email);

        if (existingUser.isPresent()) {
            UserDto utilisateur = existingUser.get();
            this.userAccountManager.addRoleIfMissing(utilisateur.getId(), Roles.ROLE_UTILISATEUR);
            return new UserAccountResult(utilisateur.getId(), false, utilisateur.isEnabled());
        }

        try {
            UserDto newUser = this.createUserAccount(email, nom, prenom, mobile);
            return new UserAccountResult(newUser.getId(), true, false);
        } catch (DataIntegrityViolationException e) {
            UserDto existingUserAfterConflict = this.userAccountManager.findByEmail(email)
                    .orElseThrow(() -> e);
            this.userAccountManager.addRoleIfMissing(existingUserAfterConflict.getId(), Roles.ROLE_UTILISATEUR);
            return new UserAccountResult(existingUserAfterConflict.getId(), false, existingUserAfterConflict.isEnabled());
        }
    }

    private UserDto createUserAccount(String email, String nom, String prenom, String mobile) {
        UserDto newUser = new UserDto();
        newUser.setEmail(email);
        newUser.setUsername(email);
        newUser.setNom(nom);
        newUser.setPrenom(prenom);
        newUser.setMobile(mobile);
        newUser.setRoles(Set.of(RoleDto.builder().role(Roles.ROLE_UTILISATEUR).build()));

        return this.userAccountManager.createUser(newUser);
    }

    /**
     * Crée une demande de mail en statut NOT_READY et la lie à la DocumentRequestEntity fournie.
     * Le mail passera automatiquement en PENDING une fois le document passé en COMPLETED.
     * Si documentRequest est null, le mail est créé directement en PENDING.
     *
     * @param idInscription    l'ID métier de l'inscription
     * @param documentRequest  la demande de document à lier (peut être null si aucun document à attendre)
     */
    protected void createMailRequest(Long idInscription, DocumentRequestEntity documentRequest) {
        MailRequestStatut statut = documentRequest != null ? MailRequestStatut.NOT_READY : MailRequestStatut.PENDING;
        MailRequestEntity mailRequest = MailRequestEntity.builder()
                .businessId(idInscription)
                .type(MailRequestType.INSCRIPTION)
                .statut(statut)
                .build();

        if (documentRequest != null) {
            mailRequest.getDocumentRequests().add(
                    MailRequestDocumentRequestEntity.builder()
                            .documentRequestId(documentRequest.getId())
                            .build()
            );
        }

        this.mailRequestRepository.save(mailRequest);
    }

    protected String generateNoInscription() {
        Long noInscription = this.inscriptionRepository.getNextNumeroInscription();
        return "AMC" + "-" + noInscription;
    }

    protected record UserAccountResult(Long userId, boolean newlyCreated, boolean enabled) {
    }
}
