package com.petadoption.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI petAdoptionOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Pet Adoption System API")
                        .version("1.0.0")
                        .description("AI Powered Pet Adoption Management System")
                        .contact(new Contact()
                                .name("Abhishek Goswami")));
    }
}