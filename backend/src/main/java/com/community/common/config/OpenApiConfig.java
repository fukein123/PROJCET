package com.community.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI communityOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Community Volunteer Service API")
                        .description("Backend API docs for Community Volunteer Service Management System")
                        .version("v1.0.0")
                        .contact(new Contact().name("fukexin123").email("3547795196@qq.com"))
                        .license(new License().name("MIT")));
    }
}

