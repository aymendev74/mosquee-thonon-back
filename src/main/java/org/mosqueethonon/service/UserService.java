package org.mosqueethonon.service;

import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    public void changeUserPassword(ChangePasswordRequest chagePasswordRequest) throws InvalidOldPasswordException;

}
