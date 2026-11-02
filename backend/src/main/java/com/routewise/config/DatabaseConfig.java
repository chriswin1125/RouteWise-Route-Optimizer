package com.routewise.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("postgres")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Bean
    public DataSource dataSource() {
        // Check for Render/Heroku standard DATABASE_URL
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null || envDbUrl.isBlank()) {
            envDbUrl = configuredUrl;
        }

        HikariConfig config = new HikariConfig();

        if (envDbUrl != null && (envDbUrl.startsWith("postgres://") || envDbUrl.startsWith("postgresql://"))) {
            try {
                String cleanUri = envDbUrl.replace("jdbc:", "");
                URI uri = new URI(cleanUri);

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "routewisedb";

                String userInfo = uri.getUserInfo();
                String username = configuredUsername;
                String password = configuredPassword;

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                log.info("Configuring PostgreSQL datasource from DATABASE_URL: jdbc:postgresql://{}:{}/{}", host, port, dbName);

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
            } catch (Exception ex) {
                log.warn("Failed to parse DATABASE_URL ({}), falling back to direct config: {}", envDbUrl, ex.getMessage());
                config.setJdbcUrl(configuredUrl);
                config.setUsername(configuredUsername);
                config.setPassword(configuredPassword);
                config.setDriverClassName("org.postgresql.Driver");
            }
        } else {
            config.setJdbcUrl(configuredUrl != null && !configuredUrl.isBlank() ? configuredUrl : "jdbc:postgresql://localhost:5432/routewisedb");
            config.setUsername(configuredUsername != null && !configuredUsername.isBlank() ? configuredUsername : "postgres");
            config.setPassword(configuredPassword != null ? configuredPassword : "postgres");
            config.setDriverClassName("org.postgresql.Driver");
        }

        // Connection pool tuning for cloud free tiers (e.g. Render / Supabase)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
