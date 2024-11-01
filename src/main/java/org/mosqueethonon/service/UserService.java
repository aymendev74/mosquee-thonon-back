package org.mosqueethonon.service;

import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {

    public void changeUserPassword(ChangePasswordRequest chagePasswordRequest) throws InvalidOldPasswordException;

    public void saveLoginHistory(String username);

    public Set<String> getAllUsernames();

}
