package org.mosqueethonon.service;

import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {

    public void changeUserPassword(ChangePasswordRequest chagePasswordRequest) throws InvalidOldPasswordException;

    public void saveLoginHistory(String username);

    public Set<String> getAllUsernames();

    public Set<String> getAllRoles();

    public UserDto createUser(UserDto user);

}
