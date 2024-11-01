package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.mosqueethonon.v1.exception.ErrorConstantes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(path = "/v1/users")
@AllArgsConstructor
public class UserController {

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

    @GetMapping
    public ResponseEntity<Set<String>> getUsers() {
        return ResponseEntity.ok(this.userService.getAllUsernames());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
        return ResponseEntity.ok(this.userService.createUser(user));
    }

}
