package com.prueba.transport_orders.service;

import com.prueba.transport_orders.dto.auth.LoginRequest;
import com.prueba.transport_orders.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}
