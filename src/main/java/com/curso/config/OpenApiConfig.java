package com.curso.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gestaoInvestimentosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestao de corretoras, acoes e carteira")
                        .description("Documentacao dos endpoints REST da API.")
                        .version("v1"));
    }
}
