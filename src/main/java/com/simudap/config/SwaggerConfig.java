package com.simudap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    public static final String SECURITY_NAME = "BearerAuth";
    private static final String PACKAGE_PATH = "com.simudap.controller";
    private static final String NEW_API_PATH_PREFIX = "/**";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
                .info(getInfo())
                .components(getSecuritySchemes())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_NAME));
    }

    @Bean
    public GroupedOpenApi newApi() {
        return GroupedOpenApi.builder()
                .group("v1")
                .packagesToScan(PACKAGE_PATH)
                .pathsToMatch(NEW_API_PATH_PREFIX)
                .build();
    }

    private Info getInfo() {
        return new Info()
                .title("Simudaq REST API")
                .description("Simudaq REST API OpenApi Documentation")
                .version("1.0")
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                )
                .contact(new Contact()
                        .name("gunyoung Park")
                        .url("https://github.com/Playaholics")
                        .email("parkky3563@gmail.com")
                );
    }

    private Components getSecuritySchemes() {
        return new Components()
                .addSecuritySchemes(
                        SECURITY_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                );
    }
}