package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping(path = "/v1/roles")
@AllArgsConstructor
public class RoleController {

    private UserService userService;

    @GetMapping
    public ResponseEntity<Set<String>> getRoles() {
        return ResponseEntity.ok(this.userService.getAllRoles());
    }

}
