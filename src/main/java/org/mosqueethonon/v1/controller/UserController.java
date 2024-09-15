package org.mosqueethonon.v1.controller;

import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.authentication.user.AuthRequest;
import org.mosqueethonon.authentication.user.AuthResponse;
import org.mosqueethonon.authentication.jwt.JwtTokenUtil;
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
    private JwtTokenUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/auth")
    public ResponseEntity authenticate(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword())
            );

            UtilisateurEntity user = (UtilisateurEntity) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(user);
            AuthResponse response = AuthResponse.builder().username(user.getUsername()).accessToken(accessToken).build();

            // On garde une trace de la connexion de l'utilisateur
            this.userService.saveLoginHistory(user.getUsername());

            return ResponseEntity.ok().body(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorConstantes.ERROR_INVALID_CREDENTIALS);
        }
    }

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
