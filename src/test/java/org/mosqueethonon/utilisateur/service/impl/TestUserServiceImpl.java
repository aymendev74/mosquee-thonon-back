package org.mosqueethonon.utilisateur.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.config.TimeConfiguration;
import org.mosqueethonon.inscription.service.InscriptionOrchestratorService;
import org.mosqueethonon.utilisateur.entity.LoginHistoryEntity;
import org.mosqueethonon.utilisateur.entity.RoleEntity;
import org.mosqueethonon.utilisateur.entity.UserAccountActionEntity;
import org.mosqueethonon.utilisateur.enums.UserAccountActionTypeEnum;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.utilisateur.exception.InvalidOldPasswordException;
import org.mosqueethonon.utilisateur.repository.LoginRepository;
import org.mosqueethonon.utilisateur.repository.UserAccountActionRepository;
import org.mosqueethonon.utilisateur.repository.RoleRepository;
import org.mosqueethonon.utilisateur.repository.UtilisateurRepository;
import org.mosqueethonon.utilisateur.entity.UtilisateurRoleEntity;
import org.mosqueethonon.utilisateur.v1.criteria.UserCriteria;
import org.mosqueethonon.utilisateur.v1.dto.AccountInfosDto;
import org.mosqueethonon.utilisateur.v1.dto.ChangePasswordDto;
import org.mosqueethonon.utilisateur.v1.dto.EnableAccountDto;
import org.mosqueethonon.utilisateur.v1.dto.ResetPasswordDto;
import org.mosqueethonon.utilisateur.v1.dto.UserDto;
import org.mosqueethonon.utilisateur.v1.dto.UserInfoDto;
import org.mosqueethonon.utilisateur.v1.mapper.UserMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private UserAccountActionRepository userAccountActionRepository;
    @Mock
    private UserAccountManager userAccountManager;
    @Mock
    private InscriptionOrchestratorService inscriptionOrchestratorService;

    // Horloge réelle sur le fuseau de l'application : comportement identique à avant
    // l'injection du Clock. Utiliser Clock.fixed(...) pour un test sensible à la date.
    @Spy
    private Clock clock = Clock.system(TimeConfiguration.ZONE_APPLICATION);

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

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateUser() {
        when(userAccountManager.createUser(any(UserDto.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userAccountManager).createUser(any(UserDto.class));
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
        UserAccountActionEntity accountAction = new UserAccountActionEntity();
        accountAction.setUsername("testuser");
        accountAction.setToken(token);
        accountAction.setType(UserAccountActionTypeEnum.ACTIVATION);
        when(userAccountActionRepository.findByTokenAndType(token, UserAccountActionTypeEnum.ACTIVATION)).thenReturn(accountAction);
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
        UserAccountActionEntity accountAction = new UserAccountActionEntity();
        accountAction.setUsername("testuser");
        accountAction.setToken(token);
        accountAction.setType(UserAccountActionTypeEnum.ACTIVATION);
        UtilisateurEntity utilisateurEntity = new UtilisateurEntity();
        utilisateurEntity.setUsername("testuser");
        utilisateurEntity.setEnabled(false);
        when(userAccountActionRepository.findByTokenAndType(token, UserAccountActionTypeEnum.ACTIVATION)).thenReturn(accountAction);
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
        verify(userAccountActionRepository).deleteByUsernameAndType("testuser", UserAccountActionTypeEnum.ACTIVATION);
        verify(userAccountActionRepository, atLeastOnce()).save(any(UserAccountActionEntity.class));
    }

    @Test
    public void testGetProfile_WithAuthenticatedUser() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        
        UtilisateurEntity utilisateurEntity = new UtilisateurEntity();
        utilisateurEntity.setUsername("testuser");
        utilisateurEntity.setPrenom("Jean");
        
        UtilisateurRoleEntity role1 = new UtilisateurRoleEntity();
        role1.setRole("ROLE_UTILISATEUR");
        UtilisateurRoleEntity role2 = new UtilisateurRoleEntity();
        role2.setRole("ROLE_ADMIN");
        
        List<UtilisateurRoleEntity> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        utilisateurEntity.setRoles(roles);
        
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateurEntity));
        
        // Act
        UserInfoDto result = userService.getProfile();
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Jean", result.getPrenom());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains("ROLE_UTILISATEUR"));
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        
        verify(utilisateurRepository).findByUsername("testuser");
    }

    @Test
    public void testGetProfile_WithAnonymousUser() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anonymousUser");
        
        // Act
        UserInfoDto result = userService.getProfile();
        
        // Assert
        assertNull(result);
        verify(utilisateurRepository, never()).findByUsername(any());
    }

    @Test
    public void testGetProfile_WithNullUsername() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(null);
        
        // Act
        UserInfoDto result = userService.getProfile();
        
        // Assert
        assertNull(result);
        verify(utilisateurRepository, never()).findByUsername(any());
    }

    @Test
    public void testGetProfile_UserNotFound() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknownuser");
        when(utilisateurRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile());
    }

    @Test
    public void testFindByEmail_MetLEmailEnMinuscule() {
        when(utilisateurRepository.findByEmail("myemail@mycompany.com")).thenReturn(Optional.of(utilisateur));
        when(userMapper.fromEntityToDto(utilisateur)).thenReturn(userDto);

        Optional<UserDto> result = userService.findByEmail("MyEmail@MyCompany.COM");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(utilisateurRepository).findByEmail("myemail@mycompany.com");
    }

    @Test
    public void testFindByEmail_Inconnu() {
        when(utilisateurRepository.findByEmail("unknown@mycompany.com")).thenReturn(Optional.empty());

        assertTrue(userService.findByEmail("unknown@mycompany.com").isEmpty());
    }

    @Test
    public void testFindByUsername() {
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));
        when(userMapper.fromEntityToDto(utilisateur)).thenReturn(userDto);

        Optional<UserDto> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    public void testAddRoleIfMissing_DelegueAuAccountManager() {
        userService.addRoleIfMissing(1L, "ROLE_ADMIN");

        verify(userAccountManager).addRoleIfMissing(1L, "ROLE_ADMIN");
    }

    @Test
    public void testGetAllRoles() {
        RoleEntity roleAdmin = new RoleEntity();
        roleAdmin.setRole("ROLE_ADMIN");
        RoleEntity roleUtilisateur = new RoleEntity();
        roleUtilisateur.setRole("ROLE_UTILISATEUR");
        when(roleRepository.findAll()).thenReturn(Arrays.asList(roleAdmin, roleUtilisateur));

        Set<String> roles = userService.getAllRoles();

        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_UTILISATEUR"));
    }

    @Test
    public void testSaveLoginHistory() {
        userService.saveLoginHistory("testuser");

        ArgumentCaptor<LoginHistoryEntity> captor = ArgumentCaptor.forClass(LoginHistoryEntity.class);
        verify(loginRepository).save(captor.capture());
        assertEquals("testuser", captor.getValue().getUsername());
        assertNotNull(captor.getValue().getDateConnexion());
    }

    @Test
    public void testFindUsersByCriteria() {
        when(utilisateurRepository.findAll(ArgumentMatchers.<Specification<UtilisateurEntity>>any()))
                .thenReturn(List.of(utilisateur));
        when(userMapper.fromEntityToDto(utilisateur)).thenReturn(userDto);

        List<UserDto> result = userService.findUsersByCriteria(new UserCriteria());

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    public void testLoadUserByUsername() {
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    public void testLoadUserByUsername_Inconnu() {
        when(utilisateurRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    public void testChangeUserPassword() throws InvalidOldPasswordException {
        mockAuthenticatedUser("testuser");
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("oldpass");
        dto.setNewPassword("newpass");
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("oldpass", "encodedpass")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("newencodedpass");

        userService.changeUserPassword(dto);

        assertEquals("newencodedpass", utilisateur.getPassword());
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    public void testChangeUserPassword_UserIntrouvable() {
        mockAuthenticatedUser("testuser");
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("oldpass");
        dto.setNewPassword("newpass");
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.changeUserPassword(dto));
        verify(utilisateurRepository, never()).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testChangeUserPassword_AncienMotDePasseInvalide() {
        mockAuthenticatedUser("testuser");
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("wrongpass");
        dto.setNewPassword("newpass");
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("wrongpass", "encodedpass")).thenReturn(false);

        assertThrows(InvalidOldPasswordException.class, () -> userService.changeUserPassword(dto));
        verify(utilisateurRepository, never()).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testDeleteUser_AvecRoleUtilisateur_SupprimeLesInscriptions() {
        UtilisateurRoleEntity role = new UtilisateurRoleEntity();
        role.setRole("ROLE_UTILISATEUR");
        utilisateur.setRoles(new ArrayList<>(List.of(role)));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        userService.deleteUser(1L);

        verify(inscriptionOrchestratorService).deleteByIdUtilisateur(1L);
        verify(loginRepository).deleteByUsername("testuser");
        verify(userAccountActionRepository).deleteByUsername("testuser");
        verify(utilisateurRepository).delete(utilisateur);
    }

    @Test
    public void testDeleteUser_SansRoleUtilisateur_NeSupprimePasLesInscriptions() {
        UtilisateurRoleEntity role = new UtilisateurRoleEntity();
        role.setRole("ROLE_ADMIN");
        utilisateur.setRoles(new ArrayList<>(List.of(role)));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        userService.deleteUser(1L);

        verify(inscriptionOrchestratorService, never()).deleteByIdUtilisateur(any());
        verify(utilisateurRepository).delete(utilisateur);
    }

    @Test
    public void testGetAccountInformations_TokenInvalide() {
        when(userAccountActionRepository.findByTokenAndType("badtoken", UserAccountActionTypeEnum.ACTIVATION)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.getAccountInformations("badtoken"));
    }

    @Test
    public void testGetAccountInformations_UtilisateurIntrouvable() {
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.ACTIVATION))
                .thenReturn(accountAction("testuser", "token123", UserAccountActionTypeEnum.ACTIVATION));
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getAccountInformations("token123"));
    }

    @Test
    public void testEnableAccount_TokenInconnu() {
        EnableAccountDto dto = new EnableAccountDto();
        dto.setUsername("testuser");
        dto.setToken("badtoken");
        dto.setPassword("pass");
        when(userAccountActionRepository.findByTokenAndType("badtoken", UserAccountActionTypeEnum.ACTIVATION)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.enableAccount(dto));
        verify(utilisateurRepository, never()).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testEnableAccount_UsernameNeCorrespondPasAuToken() {
        EnableAccountDto dto = new EnableAccountDto();
        dto.setUsername("autreuser");
        dto.setToken("token123");
        dto.setPassword("pass");
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.ACTIVATION))
                .thenReturn(accountAction("testuser", "token123", UserAccountActionTypeEnum.ACTIVATION));

        assertThrows(ResourceNotFoundException.class, () -> userService.enableAccount(dto));
        verify(utilisateurRepository, never()).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testResendActivationMail_UtilisateurIntrouvable() {
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.resendActivationMail(2L));
    }

    @Test
    public void testResendActivationMail_CompteDejaActive() {
        utilisateur.setEnabled(true);
        utilisateur.setEmail("test@domain.com");
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        assertThrows(IllegalStateException.class, () -> userService.resendActivationMail(1L));
        verify(userAccountActionRepository, never()).save(any(UserAccountActionEntity.class));
    }

    @Test
    public void testResendActivationMail_EmailInconnu() {
        utilisateur.setEmail(null);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        assertThrows(IllegalStateException.class, () -> userService.resendActivationMail(1L));
        verify(userAccountActionRepository, never()).save(any(UserAccountActionEntity.class));
    }

    @Test
    public void testRequestResetPassword() {
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));

        userService.requestResetPassword("testuser");

        verify(userAccountActionRepository).deleteByUsernameAndType("testuser", UserAccountActionTypeEnum.RESET_PASSWORD);
        ArgumentCaptor<UserAccountActionEntity> captor = ArgumentCaptor.forClass(UserAccountActionEntity.class);
        verify(userAccountActionRepository).save(captor.capture());
        assertEquals("testuser", captor.getValue().getUsername());
        assertEquals(UserAccountActionTypeEnum.RESET_PASSWORD, captor.getValue().getType());
        assertNotNull(captor.getValue().getToken());
    }

    @Test
    public void testRequestResetPassword_UtilisateurInconnu_NeFaitRienSilencieusement() {
        when(utilisateurRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Aucune exception : on ne doit pas révéler qu'un compte existe ou non
        userService.requestResetPassword("unknown");

        verify(userAccountActionRepository, never()).deleteByUsernameAndType(any(), any());
        verify(userAccountActionRepository, never()).save(any(UserAccountActionEntity.class));
    }

    @Test
    public void testGetResetPasswordInfo() {
        utilisateur.setEnabled(true);
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.RESET_PASSWORD))
                .thenReturn(accountAction("testuser", "token123", UserAccountActionTypeEnum.RESET_PASSWORD));
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));

        AccountInfosDto infos = userService.getResetPasswordInfo("token123");

        assertEquals("testuser", infos.getUsername());
        assertTrue(infos.isEnabled());
    }

    @Test
    public void testGetResetPasswordInfo_TokenInvalide() {
        when(userAccountActionRepository.findByTokenAndType("badtoken", UserAccountActionTypeEnum.RESET_PASSWORD)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.getResetPasswordInfo("badtoken"));
    }

    @Test
    public void testGetResetPasswordInfo_UtilisateurIntrouvable() {
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.RESET_PASSWORD))
                .thenReturn(accountAction("testuser", "token123", UserAccountActionTypeEnum.RESET_PASSWORD));
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getResetPasswordInfo("token123"));
    }

    @Test
    public void testResetPassword() {
        UserAccountActionEntity action = accountAction("testuser", "token123", UserAccountActionTypeEnum.RESET_PASSWORD);
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setUsername("testuser");
        dto.setToken("token123");
        dto.setPassword("newpass");
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.RESET_PASSWORD)).thenReturn(action);
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode("newpass")).thenReturn("newencoded");

        userService.resetPassword(dto);

        assertEquals("newencoded", utilisateur.getPassword());
        verify(utilisateurRepository).save(utilisateur);
        // La demande de reset doit être consommée
        verify(userAccountActionRepository).delete(action);
    }

    @Test
    public void testResetPassword_TokenInconnu() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setUsername("testuser");
        dto.setToken("badtoken");
        dto.setPassword("newpass");
        when(userAccountActionRepository.findByTokenAndType("badtoken", UserAccountActionTypeEnum.RESET_PASSWORD)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.resetPassword(dto));
        verify(utilisateurRepository, never()).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testResetPassword_UsernameNeCorrespondPasAuToken() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setUsername("autreuser");
        dto.setToken("token123");
        dto.setPassword("newpass");
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.RESET_PASSWORD))
                .thenReturn(accountAction("testuser", "token123", UserAccountActionTypeEnum.RESET_PASSWORD));

        assertThrows(ResourceNotFoundException.class, () -> userService.resetPassword(dto));
        verify(utilisateurRepository, never()).save(any(UtilisateurEntity.class));
    }

    @Test
    public void testResetPassword_UtilisateurIntrouvable() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setUsername("testuser");
        dto.setToken("token123");
        dto.setPassword("newpass");
        when(userAccountActionRepository.findByTokenAndType("token123", UserAccountActionTypeEnum.RESET_PASSWORD))
                .thenReturn(accountAction("testuser", "token123", UserAccountActionTypeEnum.RESET_PASSWORD));
        when(utilisateurRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.resetPassword(dto));
    }

    private UserAccountActionEntity accountAction(String username, String token, UserAccountActionTypeEnum type) {
        UserAccountActionEntity accountAction = new UserAccountActionEntity();
        accountAction.setUsername(username);
        accountAction.setToken(token);
        accountAction.setType(type);
        return accountAction;
    }

    private void mockAuthenticatedUser(String username) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
    }

}
