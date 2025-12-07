package org.mosqueethonon.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.mail.MailingActivationUtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.LoginRepository;
import org.mosqueethonon.repository.MailingActivationUtilisateurRepository;
import org.mosqueethonon.repository.RoleRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.v1.dto.account.AccountInfosDto;
import org.mosqueethonon.v1.dto.account.EnableAccountDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.mapper.user.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestUserServiceImpl {
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginRepository loginRepository;
    @Mock
    private MailingActivationUtilisateurRepository mailingActivationUtilisateurRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UtilisateurEntity utilisateur;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        utilisateur = new UtilisateurEntity();
        utilisateur.setId(1L);
        utilisateur.setUsername("testuser");
        utilisateur.setPassword("encodedpass");
        utilisateur.setEnabled(false);
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("myemail@mycompany.com");
        userDto.setNom("myname");
        userDto.setPrenom("myfirstname");
    }

    @Test
    public void testCreateUser() {
        when(userMapper.fromDtoToEntity(any(UserDto.class))).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpass");
        when(userMapper.fromEntityToDto(any(UtilisateurEntity.class))).thenReturn(userDto);
        when(utilisateurRepository.save(any(UtilisateurEntity.class))).thenReturn(utilisateur);

        UserDto result = userService.createUser(userDto);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(utilisateurRepository).save(any(UtilisateurEntity.class));
        verify(mailingActivationUtilisateurRepository).save(any());
    }

    @Test
    public void testUpdateUser() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(userMapper.fromEntityToDto(any(UtilisateurEntity.class))).thenReturn(userDto);
        when(utilisateurRepository.save(any(UtilisateurEntity.class))).thenReturn(utilisateur);

        UserDto result = userService.updateUser(1L, userDto);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(utilisateurRepository).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testDeleteUser() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        userService.deleteUser(1L);
        verify(utilisateurRepository).delete(utilisateur);
    }

    @Test
    public void testUpdateUser_NotFound() {
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(2L, userDto));
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(2L));
    }

    @Test
    public void testGetAccountInformations() {
        String token = "token123";
        UtilisateurEntity utilisateurEntity = new UtilisateurEntity();
        utilisateurEntity.setUsername("testuser");
        utilisateurEntity.setEnabled(false);
        MailingActivationUtilisateurEntity mailActivation = new MailingActivationUtilisateurEntity();
        mailActivation.setUsername("testuser");
        mailActivation.setToken(token);
        when(mailingActivationUtilisateurRepository.findByToken(token)).thenReturn(mailActivation);
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateurEntity));
        AccountInfosDto infos = userService.getAccountInformations(token);
        assertEquals("testuser", infos.getUsername());
        assertFalse(infos.isEnabled());
    }

    @Test
    public void testEnableAccount() {
        String token = "token123";
        EnableAccountDto dto = new EnableAccountDto();
        dto.setUsername("testuser");
        dto.setToken(token);
        dto.setPassword("pass");
        MailingActivationUtilisateurEntity mailActivation = new MailingActivationUtilisateurEntity();
        mailActivation.setUsername("testuser");
        mailActivation.setToken(token);
        UtilisateurEntity utilisateurEntity = new UtilisateurEntity();
        utilisateurEntity.setUsername("testuser");
        utilisateurEntity.setEnabled(false);
        when(mailingActivationUtilisateurRepository.findByToken(token)).thenReturn(mailActivation);
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateurEntity));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        userService.enableAccount(dto);
        assertTrue(utilisateurEntity.isEnabled());
        assertEquals("encoded", utilisateurEntity.getPassword());
        verify(utilisateurRepository).save(utilisateurEntity);
    }

    @Test
    public void testResendActivationMail() {
        UtilisateurEntity utilisateurEntity = new UtilisateurEntity();
        utilisateurEntity.setId(1L);
        utilisateurEntity.setUsername("testuser");
        utilisateurEntity.setEnabled(false);
        utilisateurEntity.setEmail("test@domain.com");
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateurEntity));
        userService.resendActivationMail(1L);
        verify(mailingActivationUtilisateurRepository).deleteByUsername("testuser");
        verify(mailingActivationUtilisateurRepository, atLeastOnce()).save(any(MailingActivationUtilisateurEntity.class));
    }
}
