package org.mosqueethonon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthentificationController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
