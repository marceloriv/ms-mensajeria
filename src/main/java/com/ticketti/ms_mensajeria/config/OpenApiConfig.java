package com.ticketti.ms_mensajeria.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String jwtSchemeName = "bearerAuth";
        SecurityScheme jwtScheme = new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);

        return new OpenAPI()
                .info(new Info()
                        .title("MS-Mensajería API")
                        .version("1.0.0")
                        .description("Microservicio de notificaciones — Ticketti")
                        .contact(new Contact()
                                .name("Equipo 7")
                                .email("notificaciones.ticketti@gmail.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Local")))
                .components(new Components().addSecuritySchemes(jwtSchemeName, jwtScheme))
                .addSecurityItem(securityRequirement);
    }
}
