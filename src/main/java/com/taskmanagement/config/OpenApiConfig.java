package com.taskmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management & Notification System API")
                        .version("1.0.0")
                        .description("Scalable Task Management Platform with Dual-Channel Notifications\n\n" +
                                "**Authentication:** This API uses JWT Bearer token authentication.\n\n" +
                                "**Steps to authenticate:**\n" +
                                "1. Register a new user via `/api/auth/register` or login via `/api/auth/login`\n" +
                                "2. Copy the JWT token from the response\n" +
                                "3. Click the 'Authorize' button and enter: `Bearer <your-token>`\n" +
                                "4. All subsequent requests will include the token automatically\n\n" +
                                "**Roles:**\n" +
                                "- **ADMIN**: Full access to all endpoints\n" +
                                "- **MANAGER**: Can manage tasks and view users\n" +
                                "- **USER**: Can view tasks and manage own data")
                        .contact(new Contact()
                                .name("Task Management Team")
                                .email("support@taskmanagement.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/auth/login or /api/auth/register")));
    }
}

