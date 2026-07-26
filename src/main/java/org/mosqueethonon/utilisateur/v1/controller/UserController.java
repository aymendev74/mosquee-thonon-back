package org.mosqueethonon.utilisateur.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.utilisateur.v1.dto.ChangePasswordRequest;
import org.mosqueethonon.utilisateur.exception.InvalidOldPasswordException;
import org.mosqueethonon.utilisateur.service.UserService;
import org.mosqueethonon.utilisateur.v1.criteria.UserCriteria;
import org.mosqueethonon.utilisateur.v1.dto.AccountInfosDto;
import org.mosqueethonon.utilisateur.v1.dto.EnableAccountDto;
import org.mosqueethonon.utilisateur.v1.dto.ResetPasswordDto;
import org.mosqueethonon.utilisateur.v1.dto.UserDto;
import org.mosqueethonon.v1.exception.ErrorConstantes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            this.userService.changeUserPassword(request);
        } catch (InvalidOldPasswordException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorConstantes.ERROR_INVALID_OLD_PASSWORD);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(@ModelAttribute UserCriteria userCriteria) {
        return ResponseEntity.ok(this.userService.findUsersByCriteria(userCriteria));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
        return ResponseEntity.ok(this.userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") Long id, @RequestBody UserDto user) {
        return ResponseEntity.ok(this.userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable("id") Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/informations")
    public ResponseEntity<AccountInfosDto> getAccountInformations(@RequestParam("token") String token) {
        return ResponseEntity.ok(this.userService.getAccountInformations(token));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enableAccount(@RequestBody EnableAccountDto enableAccountDto) {
        this.userService.enableAccount(enableAccountDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/activationMail")
    public ResponseEntity<?> resendActivationMail(@PathVariable("id") Long idUtilisateur) {
        this.userService.resendActivationMail(idUtilisateur);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/resetPassword/request")
    public ResponseEntity<?> requestResetPassword(@RequestBody JsonNode body) {
        this.userService.requestResetPassword(body.get("username").asText());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/resetPassword/informations")
    public ResponseEntity<AccountInfosDto> getResetPasswordInfo(@RequestParam("token") String token) {
        return ResponseEntity.ok(this.userService.getResetPasswordInfo(token));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        this.userService.resetPassword(resetPasswordDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
