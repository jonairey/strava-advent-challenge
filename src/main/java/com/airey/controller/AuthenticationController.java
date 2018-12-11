package com.airey.controller;

import com.airey.domain.authorization.Authorization;
import com.airey.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping("/{id}")
    public Authorization retrieve(@PathVariable final String id) {
        return authenticationService.authenticate(id);
    }
}
