package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.dto.auth.LoginRequest;
import com.prueba.transport_orders.dto.auth.LoginResponse;
import com.prueba.transport_orders.security.JwtService;
import com.prueba.transport_orders.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtService.generateToken(authentication.getName());
        log.info("Login exitoso usuario={}", authentication.getName());
        return new LoginResponse(token, jwtService.getExpirationMs() / 1000);
    }

}
