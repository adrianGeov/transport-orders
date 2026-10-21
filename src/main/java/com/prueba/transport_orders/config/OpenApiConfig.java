package com.prueba.transport_orders.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Transport Orders API",
                version = "1.0.0",
                description = "API REST para la gestión de órdenes de transporte, conductores y asignaciones"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc5MjU2MDI2MiwiZXhwIjoxNzkyNTYzODYyfQ.4bDNI2umlOsCEWgVPjaFf8J3qOsCEB3klDkwnIINhkI")

public class OpenApiConfig {

}
