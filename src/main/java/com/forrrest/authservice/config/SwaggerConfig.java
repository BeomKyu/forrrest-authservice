package com.forrrest.authservice.config;

import com.forrrest.authservice.utils.cookies.CookieUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSecuritySchemes(CookieUtils.COOKIE_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name(CookieUtils.COOKIE_NAME)   // "forrrest_user_refreshToken"
                        ))
                .security(List.of(new SecurityRequirement().addList("bearer-token")))
                .info(new Info()
                        .title("Auth Service API")
                        .description("Auth Service API 명세서")
                        .version("1.0.0"));
    }
}