package io.goorm.board.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Board API")
                .version("1.0")
                .description("세션 기반 + JWT 기반 인증을 지원하는 게시판 API"))
            .components(new Components()
                // 세션 기반 인증용 (문서화 목적)
                .addSecuritySchemes("sessionAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID")
                        .description("세션 기반 인증 (브라우저에서 자동 처리)"))

                // JWT 기반 인증용 (문서화 + 테스트 목적)
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰 인증 (Authorization 헤더)"))
            );
    }
}