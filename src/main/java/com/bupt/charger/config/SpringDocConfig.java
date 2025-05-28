package com.bupt.charger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置Swagger文档
 */
@Configuration
public class SpringDocConfig {

    private String title = "BUPT Charge System API Documentation";
    private String version = "v1.0";

    @Bean
    public OpenAPI chargerOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(title)
                        .version(version));
    }
}