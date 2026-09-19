package com.instituto.matricula.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema D — Matrícula Académica API")
                        .version("1.0.0")
                        .description("API REST para el sistema de matrícula académica con cupos, listas de espera y prerrequisitos.")
                        .contact(new Contact()
                                .name("Instituto Técnico")
                                .email("soporte@instituto.edu")));
    }
}
