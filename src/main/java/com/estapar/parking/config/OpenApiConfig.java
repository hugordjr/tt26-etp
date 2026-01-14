package com.estapar.parking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI parkingOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Parking API")
                .version("v1")
                .description("API de gerenciamento de estacionamento"));
  }
}
