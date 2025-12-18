package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.entity.mail.MailingActivationUtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.LoginHistoryEntity;
import org.mosqueethonon.entity.utilisateur.RoleEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.LoginRepository;
import org.mosqueethonon.repository.MailingActivationUtilisateurRepository;
import org.mosqueethonon.repository.RoleRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.repository.specifications.UserSpecifications;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.utils.PasswordGenerator;
import org.mosqueethonon.utils.UserActivationTokenGenerator;
import org.mosqueethonon.v1.criterias.UserCriteria;
import org.mosqueethonon.v1.dto.account.AccountInfosDto;
import org.mosqueethonon.v1.dto.account.EnableAccountDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.mapper.user.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    private MailingActivationUtilisateurRepository mailingActivationUtilisateurRepository;

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
        // On créé le nouvel utilisateur
        user.normalize();
        UtilisateurEntity utilisateurEntity = this.userMapper.fromDtoToEntity(user);
        utilisateurEntity.setPassword(this.passwordEncoder.encode(PasswordGenerator.generateRandomPassword(8)));
        utilisateurEntity.setEnabled(false); // par défaut disabled
        user = this.userMapper.fromEntityToDto(this.userRepository.save(utilisateurEntity));
        // On demande un envoi d'e-mail d'activation
        this.requestMailActivation(user.getUsername());
        return user;
    }

    private void requestMailActivation(String username) {
        MailingActivationUtilisateurEntity mailingActivationUtilisateurEntity = new MailingActivationUtilisateurEntity();
        mailingActivationUtilisateurEntity.setUsername(username);
        mailingActivationUtilisateurEntity.setStatut(MailRequestStatut.PENDING);
        mailingActivationUtilisateurEntity.setToken(UserActivationTokenGenerator.generateToken(32));
        this.mailingActivationUtilisateurRepository.save(mailingActivationUtilisateurEntity);
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
        this.loginRepository.deleteByUsername(utilisateurEntity.getUsername());
        this.mailingActivationUtilisateurRepository.deleteByUsername(utilisateurEntity.getUsername());
        this.userRepository.delete(utilisateurEntity);
    }

    @Override
    public AccountInfosDto getAccountInformations(String token) {
        MailingActivationUtilisateurEntity mailActivation = this.mailingActivationUtilisateurRepository.findByToken(token);
        if(mailActivation == null) {
            throw new ResourceNotFoundException("Le token est invalide, aucun compte lié");
        }
        UtilisateurEntity user = this.userRepository.findByUsername(mailActivation.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - username = " + mailActivation.getUsername()));
        return AccountInfosDto.builder()
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .build();
    }

    @Override
    public void enableAccount(EnableAccountDto enableAccountDto) {
        MailingActivationUtilisateurEntity mailActivation = this.mailingActivationUtilisateurRepository.findByToken(enableAccountDto.getToken());
        if(mailActivation == null || !mailActivation.getUsername().equals(enableAccountDto.getUsername())) {
            throw new ResourceNotFoundException("Les paramètres d'activation sont invalides (username/token) !");
        }
        UtilisateurEntity user = this.userRepository.findByUsername(mailActivation.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - username = " + mailActivation.getUsername()));
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
        this.mailingActivationUtilisateurRepository.deleteByUsername(utilisateurEntity.getUsername());
        // on demande un nouveau mail d'activation du compte
        this.requestMailActivation(utilisateurEntity.getUsername());
    }
}
