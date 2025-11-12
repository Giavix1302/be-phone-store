package fit.se.be_phone_store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database Configuration (Simplified)
 * Let Spring Boot handle EntityManagerFactory autoconfiguration
 */
@Configuration
@EnableJpaRepositories(basePackages = "fit.se.be_phone_store.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // No custom beans - let Spring Boot autoconfigure everything
    // This prevents conflicts with EntityManagerFactory creation
}