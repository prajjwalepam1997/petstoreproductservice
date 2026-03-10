package com.chtrembl.petstore.product.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecretConfig {

    @Value("${azure.keyvault.uri:}")
    private String keyVaultUri;

    @Value("${azure.keyvault.enabled:false}")
    private boolean keyVaultEnabled;

    @Bean
    public SecretClient secretClient() {
        // Use DefaultAzureCredential for Azure environment (managed identity, CLI, etc.)
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        return new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(credential)
                .buildClient();
    }

    @Bean
    @Primary
    public MapPropertySource keyVaultPropertySource(ConfigurableEnvironment environment, SecretClient secretClient) {
        Map<String, Object> secrets = new HashMap<>();

        if (keyVaultEnabled && keyVaultUri != null && !keyVaultUri.isEmpty()) {
            // Fetch database secrets from Key Vault
            secrets.put("spring.datasource.url", getSecret(secretClient, "POSTGRESURL"));
            secrets.put("spring.datasource.username", getSecret(secretClient, "POSTGRESUSER"));
            secrets.put("spring.datasource.password", getSecret(secretClient, "POSTGRESPASSWORD"));
        }

        MapPropertySource propertySource = new MapPropertySource("keyVaultPropertySource", secrets);
        environment.getPropertySources().addFirst(propertySource);

        return propertySource;
    }

    @Bean
    @Primary
    public DataSource dataSource(SecretClient secretClient) {
        if (keyVaultEnabled && keyVaultUri != null && !keyVaultUri.isEmpty()) {
            String url = getSecret(secretClient, "POSTGRESURL");
            String username = getSecret(secretClient, "POSTGRESUSER");
            String password = getSecret(secretClient, "POSTGRESPASSWORD");

            if (url != null && username != null && password != null) {
                return DataSourceBuilder.create()
                        .driverClassName("org.postgresql.Driver")
                        .url(url)
                        .username(username)
                        .password(password)
                        .build();
            }
        }
        throw new RuntimeException("Database configuration not found. Ensure Key Vault is enabled and contains POSTGRESURL, POSTGRESUSER, and POSTGRESPASSWORD secrets.");
    }

    private String getSecret(SecretClient secretClient, String secretName) {
        try {
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            return secret.getValue();
        } catch (Exception e) {
            // Return null if secret not found, will fall back to env variables
            return null;
        }
    }
}