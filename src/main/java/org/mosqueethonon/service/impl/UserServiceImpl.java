package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.entity.mail.UserAccountActionEntity;
import org.mosqueethonon.entity.utilisateur.LoginHistoryEntity;
import org.mosqueethonon.entity.utilisateur.RoleEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.UserAccountActionType;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.*;
import org.mosqueethonon.repository.specifications.UserSpecifications;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.service.inscription.InscriptionOrchestratorService;
import org.mosqueethonon.utils.PasswordGenerator;
import org.mosqueethonon.utils.UserActivationTokenGenerator;
import org.mosqueethonon.v1.criterias.UserCriteria;
import org.mosqueethonon.v1.dto.account.AccountInfosDto;
import org.mosqueethonon.v1.dto.account.EnableAccountDto;
import org.mosqueethonon.v1.dto.account.ResetPasswordDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.dto.user.UserInfoDto;
import org.mosqueethonon.v1.mapper.user.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UtilisateurRepository userRepository;

    private RoleRepository roleRepository;

    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;

    private LoginRepository loginRepository;

    private UserAccountActionRepository userAccountActionRepository;

    private InscriptionOrchestratorService inscriptionOrchestratorService;

    private UserAccountManager userAccountManager;

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return this.userRepository.findByEmail(email.toLowerCase())
                .map(this.userMapper::fromEntityToDto);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return this.userRepository.findByUsername(username)
                .map(this.userMapper::fromEntityToDto);
    }

    @Override
    @Transactional
    public void addRoleIfMissing(Long userId, String role) {
        this.userAccountManager.addRoleIfMissing(userId, role);
    }

    @Override
    @Transactional
    public void changeUserPassword(ChangePasswordRequest changePasswordRequest) throws InvalidOldPasswordException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Le user ne contient pas le mot de passe apparement... (sûrement une sécurité spring)
        // Je reload le user depuis la DB pour avoir le mot de passe.
        UtilisateurEntity user = this.userRepository.findByUsername(username).orElse(null);
        if(user == null) {
            // Pas normal, le user existe forcément puisqu'il a été authentifié si on est arrivée là
            throw new IllegalStateException("Le user n'a pas été retrouvé dans la base de données");
        }
        if(!validateOldPassword(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidOldPasswordException();
        }
        user.setPassword(this.passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User " + username + " not found."));
    }

    private boolean validateOldPassword(String rawPassword, String encodedPassword) {
        return this.passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    @Transactional
    public void saveLoginHistory(String username) {
        LoginHistoryEntity loginHistory = new LoginHistoryEntity();
        loginHistory.setUsername(username);
        loginHistory.setDateConnexion(LocalDateTime.now());
        loginRepository.save(loginHistory);
    }

    @Override
    public List<UserDto> findUsersByCriteria(UserCriteria userCriteria) {
        return this.userRepository.findAll(UserSpecifications.withCriteria(userCriteria)).stream().map(this.userMapper::fromEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAllRoles() {
        return this.roleRepository.findAll().stream().map(RoleEntity::getRole)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto user) {
        return this.userAccountManager.createUser(user);
    }

    private void requestAccountAction(String username, UserAccountActionType type) {
        UserAccountActionEntity accountAction = new UserAccountActionEntity();
        accountAction.setUsername(username);
        accountAction.setStatut(MailRequestStatut.PENDING);
        accountAction.setType(type);
        accountAction.setToken(UserActivationTokenGenerator.generateToken(32));
        this.userAccountActionRepository.save(accountAction);
    }

    @Override
    public UserDto updateUser(Long id, UserDto user) {
        UtilisateurEntity utilisateurEntity = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - id = " + id));
        user.normalize();
        this.userMapper.updateUserEntityFromDto(user, utilisateurEntity);
        user = this.userMapper.fromEntityToDto(this.userRepository.save(utilisateurEntity));
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        UtilisateurEntity utilisateurEntity = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - id = " + id));
        
        if (this.hasRole(utilisateurEntity, ROLE_UTILISATEUR)) {
            this.inscriptionOrchestratorService.deleteByIdUtilisateur(utilisateurEntity.getId());
        }
        
        this.loginRepository.deleteByUsername(utilisateurEntity.getUsername());
        this.userAccountActionRepository.deleteByUsername(utilisateurEntity.getUsername());
        this.userRepository.delete(utilisateurEntity);
    }

    private boolean hasRole(UtilisateurEntity utilisateur, String role) {
        return utilisateur.getRoles() != null && utilisateur.getRoles().stream()
                .anyMatch(r -> role.equals(r.getRole()));
    }

    private static final String ROLE_UTILISATEUR = "ROLE_UTILISATEUR";

    @Override
    public AccountInfosDto getAccountInformations(String token) {
        UserAccountActionEntity accountAction = this.userAccountActionRepository.findByTokenAndType(token, UserAccountActionType.ACTIVATION);
        if(accountAction == null) {
            throw new ResourceNotFoundException("Le token est invalide, aucun compte lié");
        }
        UtilisateurEntity user = this.userRepository.findByUsername(accountAction.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - username = " + accountAction.getUsername()));
        return AccountInfosDto.builder()
                .username(user.getUsername())
                .prenom(user.getPrenom())
                .enabled(user.isEnabled())
                .build();
    }

    @Override
    public void enableAccount(EnableAccountDto enableAccountDto) {
        UserAccountActionEntity accountAction = this.userAccountActionRepository.findByTokenAndType(enableAccountDto.getToken(), UserAccountActionType.ACTIVATION);
        if(accountAction == null || !accountAction.getUsername().equals(enableAccountDto.getUsername())) {
            throw new ResourceNotFoundException("Les paramètres d'activation sont invalides (username/token) !");
        }
        UtilisateurEntity user = this.userRepository.findByUsername(accountAction.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - username = " + accountAction.getUsername()));
        user.setEnabled(true);
        user.setPassword(this.passwordEncoder.encode(enableAccountDto.getPassword()));
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendActivationMail(Long idUtilisateur) {
        UtilisateurEntity utilisateurEntity = this.userRepository.findById(idUtilisateur)
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - id = " + idUtilisateur));
        if(utilisateurEntity.isEnabled()) {
            throw new IllegalStateException("L'utilisateur est déjà activé - id = " + idUtilisateur);
        }
        if(utilisateurEntity.getEmail() == null) {
            throw new IllegalStateException("L'email de l'utilisateur est inconnu - id = " + idUtilisateur);
        }
        // Si un mail d'activation avait déjà été envoyé, on supprime la demande
        this.userAccountActionRepository.deleteByUsernameAndType(utilisateurEntity.getUsername(), UserAccountActionType.ACTIVATION);
        // on demande un nouveau mail d'activation du compte
        this.requestAccountAction(utilisateurEntity.getUsername(), UserAccountActionType.ACTIVATION);
    }

    @Override
    @Transactional
    public void requestResetPassword(String username) {
        Optional<UtilisateurEntity> utilisateurOptional = this.userRepository.findByUsername(username);
        if(utilisateurOptional.isEmpty()) {
            return;
        }
        UtilisateurEntity utilisateur = utilisateurOptional.get();
        // Supprimer les éventuelles demandes de reset précédentes
        this.userAccountActionRepository.deleteByUsernameAndType(utilisateur.getUsername(), UserAccountActionType.RESET_PASSWORD);
        // Créer une nouvelle demande de reset
        this.requestAccountAction(utilisateur.getUsername(), UserAccountActionType.RESET_PASSWORD);
    }

    @Override
    public AccountInfosDto getResetPasswordInfo(String token) {
        UserAccountActionEntity accountAction = this.userAccountActionRepository.findByTokenAndType(token, UserAccountActionType.RESET_PASSWORD);
        if(accountAction == null) {
            throw new ResourceNotFoundException("Le token de réinitialisation est invalide");
        }
        UtilisateurEntity user = this.userRepository.findByUsername(accountAction.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - username = " + accountAction.getUsername()));
        return AccountInfosDto.builder()
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        UserAccountActionEntity accountAction = this.userAccountActionRepository.findByTokenAndType(resetPasswordDto.getToken(), UserAccountActionType.RESET_PASSWORD);
        if(accountAction == null || !accountAction.getUsername().equals(resetPasswordDto.getUsername())) {
            throw new ResourceNotFoundException("Les paramètres de réinitialisation sont invalides (username/token) !");
        }
        UtilisateurEntity user = this.userRepository.findByUsername(accountAction.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - username = " + accountAction.getUsername()));
        user.setPassword(this.passwordEncoder.encode(resetPasswordDto.getPassword()));
        this.userRepository.save(user);
        // Supprimer la demande de reset une fois le mot de passe changé
        this.userAccountActionRepository.delete(accountAction);
    }

    @Override
    public UserInfoDto getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Retourner null si aucun utilisateur connecté ou utilisateur anonyme
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }

        UtilisateurEntity utilisateur = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + username));

        List<String> roles = utilisateur.getRoles().stream()
                .map(UtilisateurRoleEntity::getRole)
                .collect(Collectors.toList());

        return UserInfoDto.builder()
                .username(utilisateur.getUsername())
                .prenom(utilisateur.getPrenom())
                .roles(roles)
                .build();
    }
}
