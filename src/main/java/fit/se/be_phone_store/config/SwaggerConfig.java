package fit.se.be_phone_store.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * Configures API documentation with security, tags, and server information
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.name:Phone E-commerce Store}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * OpenAPI Configuration Bean
     */
    @Bean
    public OpenAPI phoneStoreOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .addSecurityItem(createSecurityRequirement())
                .components(createComponents())
                .tags(createTags());
    }

    /**
     * Create API Information
     */
    private Info createApiInfo() {
        return new Info()
                .title(appName + " API")
                .description("""
                        RESTful API for Phone E-commerce Store
                        
                        ## Features
                        - 🔐 User Authentication & Authorization (JWT)
                        - 📱 Product Management (CRUD operations)
                        - 🛒 Shopping Cart & Order Management
                        - 🖼️ Image Upload with Cloudinary
                        - ⭐ Review & Rating System
                        - 🏷️ Category & Brand Management
                        - 🎨 Color Management
                        
                        ## Authentication
                        Most endpoints require authentication. Use the login endpoint to get a JWT token,
                        then include it in the Authorization header as: `Bearer <your_token>`
                        
                        ## Error Handling
                        The API uses standard HTTP status codes and returns error details in JSON format.
                        
                        ## Rate Limiting
                        API requests are rate-limited to prevent abuse. See response headers for current limits.
                        """)
                .version(appVersion)
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * Create Contact Information
     */
    private Contact createContact() {
        return new Contact()
                .name("Phone Store Development Team")
                .email("dev@phonestore.com")
                .url("https://github.com/your-username/phone-ecommerce-backend");
    }

    /**
     * Create License Information
     */
    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Create Server Configuration
     */
    private List<Server> createServers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Development Server"),
                new Server()
                        .url("https://api.phonestore.com" + contextPath)
                        .description("Production Server"),
                new Server()
                        .url("https://staging-api.phonestore.com" + contextPath)
                        .description("Staging Server")
        );
    }

    /**
     * Create Security Requirements
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }

    /**
     * Create Security Components
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", createSecurityScheme());
    }

    /**
     * Create Security Scheme for JWT
     */
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter JWT token in the format: Bearer <your_token>");
    }

    /**
     * Create API Tags for grouping endpoints
     */
    private List<Tag> createTags() {
        return Arrays.asList(
                new Tag()
                        .name("Authentication")
                        .description("User authentication and authorization endpoints"),

                new Tag()
                        .name("Products")
                        .description("Product management and browsing endpoints"),

                new Tag()
                        .name("Categories")
                        .description("Product category management endpoints"),

                new Tag()
                        .name("Brands")
                        .description("Brand management endpoints"),

                new Tag()
                        .name("Colors")
                        .description("Color management endpoints"),

                new Tag()
                        .name("Cart")
                        .description("Shopping cart management endpoints"),

                new Tag()
                        .name("Orders")
                        .description("Order processing and management endpoints"),

                new Tag()
                        .name("Reviews")
                        .description("Product review and rating endpoints"),

                new Tag()
                        .name("Images")
                        .description("Image upload and management endpoints"),

                new Tag()
                        .name("Admin")
                        .description("Administrative endpoints (Admin access required)"),

                new Tag()
                        .name("Public")
                        .description("Public endpoints (No authentication required)")
        );
    }
}