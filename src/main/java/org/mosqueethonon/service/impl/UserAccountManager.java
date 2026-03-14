package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.mosqueethonon.entity.mail.UserAccountActionEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.UserAccountActionType;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.UserAccountActionRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.utils.PasswordGenerator;
import org.mosqueethonon.utils.UserActivationTokenGenerator;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.mapper.user.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@AllArgsConstructor
public class UserAccountManager {

    private UtilisateurRepository userRepository;

    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;

    private UserAccountActionRepository userAccountActionRepository;

    public Optional<UserDto> findByEmail(String email) {
        return this.userRepository.findFirstByEmail(email.toLowerCase())
                .map(this.userMapper::fromEntityToDto);
    }

    public Optional<UserDto> findByUsername(String username) {
        return this.userRepository.findByUsername(username)
                .map(this.userMapper::fromEntityToDto);
    }

    @Transactional
    public void addRoleIfMissing(Long userId, String role) {
        UtilisateurEntity utilisateur = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur n'a pas été retrouvé - id = " + userId));
        boolean hasRole = utilisateur.getRoles() != null && utilisateur.getRoles().stream()
                .anyMatch(r -> role.equals(r.getRole()));
        if (!hasRole) {
            UtilisateurRoleEntity newRole = new UtilisateurRoleEntity();
            newRole.setRole(role);
            utilisateur.getRoles().add(newRole);
            this.userRepository.save(utilisateur);
        }
    }

    @Transactional
    public UserDto createUser(UserDto user) {
        user.normalize();
        UtilisateurEntity utilisateurEntity = this.userMapper.fromDtoToEntity(user);
        utilisateurEntity.setPassword(this.passwordEncoder.encode(PasswordGenerator.generateRandomPassword(8)));
        utilisateurEntity.setEnabled(false);
        user = this.userMapper.fromEntityToDto(this.userRepository.save(utilisateurEntity));
        this.requestMailActivation(user.getUsername());
        return user;
    }

    private void requestMailActivation(String username) {
        UserAccountActionEntity accountAction = new UserAccountActionEntity();
        accountAction.setUsername(username);
        accountAction.setStatut(MailRequestStatut.PENDING);
        accountAction.setType(UserAccountActionType.ACTIVATION);
        accountAction.setToken(UserActivationTokenGenerator.generateToken(32));
        this.userAccountActionRepository.save(accountAction);
    }
}
