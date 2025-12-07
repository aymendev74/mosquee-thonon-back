package org.mosqueethonon.service;

import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.v1.criterias.UserCriteria;
import org.mosqueethonon.v1.dto.account.AccountInfosDto;
import org.mosqueethonon.v1.dto.account.EnableAccountDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

public interface UserService extends UserDetailsService {

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

}
