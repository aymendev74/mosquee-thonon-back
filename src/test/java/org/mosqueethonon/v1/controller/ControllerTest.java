package org.mosqueethonon.v1.controller;

import org.mosqueethonon.authentication.jwt.JwtTokenUtil;
import org.mosqueethonon.entity.UtilisateurEntity;
import org.springframework.beans.factory.annotation.Autowired;

public class ControllerTest {


    @Autowired
    private JwtTokenUtil tokenUtil;

    protected String generateToken() {
        UtilisateurEntity user = new UtilisateurEntity();
        user.setId(1L);
        user.setUsername("aymen");
        return new StringBuilder("Bearer ").append(tokenUtil.generateAccessToken(user)).toString();
    }

}
