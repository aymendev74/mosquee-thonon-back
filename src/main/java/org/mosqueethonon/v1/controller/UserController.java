package org.mosqueethonon.v1.controller;

import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.authentication.user.AuthRequest;
import org.mosqueethonon.authentication.user.AuthResponse;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.v1.exception.ErrorConstantes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/user")
public class UserController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserService userService;

    @PostMapping("/password")
    public ResponseEntity changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            this.userService.changeUserPassword(request);
        } catch (InvalidOldPasswordException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorConstantes.ERROR_INVALID_OLD_PASSWORD);
        }
        return ResponseEntity.ok(true);
    }
}
