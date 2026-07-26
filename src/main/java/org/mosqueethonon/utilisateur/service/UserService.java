package org.mosqueethonon.utilisateur.service;

import org.mosqueethonon.utilisateur.v1.dto.ChangePasswordRequest;
import org.mosqueethonon.utilisateur.exception.InvalidOldPasswordException;
import org.mosqueethonon.utilisateur.v1.criteria.UserCriteria;
import org.mosqueethonon.utilisateur.v1.dto.AccountInfosDto;
import org.mosqueethonon.utilisateur.v1.dto.EnableAccountDto;
import org.mosqueethonon.utilisateur.v1.dto.ResetPasswordDto;
import org.mosqueethonon.utilisateur.v1.dto.UserDto;
import org.mosqueethonon.utilisateur.v1.dto.UserInfoDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService extends UserDetailsService {

    public Optional<UserDto> findByEmail(String email);

    public Optional<UserDto> findByUsername(String username);

    public void addRoleIfMissing(Long userId, String role);

    public void changeUserPassword(ChangePasswordRequest chagePasswordRequest) throws InvalidOldPasswordException;

    public void saveLoginHistory(String username);

    public List<UserDto> findUsersByCriteria(UserCriteria userCriteria);

    public Set<String> getAllRoles();

    public UserDto createUser(UserDto user);

    public UserDto updateUser(Long id, UserDto user);

    public void deleteUser(Long id);

    public void enableAccount(EnableAccountDto enableAccountDto);

    public void resendActivationMail(Long idUtilisateur);

    public AccountInfosDto getAccountInformations(String token);

    public void requestResetPassword(String email);

    public AccountInfosDto getResetPasswordInfo(String token);

    public void resetPassword(ResetPasswordDto resetPasswordDto);

    public UserInfoDto getProfile();

}
