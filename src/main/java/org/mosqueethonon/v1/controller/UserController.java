package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.exception.InvalidOldPasswordException;
import org.mosqueethonon.service.UserService;
import org.mosqueethonon.v1.criterias.UserCriteria;
import org.mosqueethonon.v1.dto.account.AccountInfosDto;
import org.mosqueethonon.v1.dto.account.EnableAccountDto;
import org.mosqueethonon.v1.dto.user.UserDto;
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

}
