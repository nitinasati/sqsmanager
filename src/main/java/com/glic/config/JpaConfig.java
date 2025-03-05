package com.glic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration class for JPA and repository settings.
 * This class enables JPA repository support and configures the base package
 * for scanning repository interfaces.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.glic.repository")
public class JpaConfig {
    
    /**
     * Default constructor for JpaConfig.
     * Required by Spring for configuration class instantiation.
     */
    public JpaConfig() {
    }
} 