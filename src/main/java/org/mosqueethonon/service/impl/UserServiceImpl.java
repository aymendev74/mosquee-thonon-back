package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.entity.utilisateur.LoginHistoryEntity;
import org.mosqueethonon.entity.utilisateur.RoleEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.repository.LoginRepository;
import org.mosqueethonon.repository.RoleRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.mapper.user.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public Set<String> getAllUsernames() {
        return this.userRepository.findAll().stream().map(UtilisateurEntity::getUsername)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllRoles() {
        return this.roleRepository.findAll().stream().map(RoleEntity::getRole)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userMapper.fromEntityToDto(this.userRepository.save(this.userMapper.fromDtoToEntity(user)));
    }
}
