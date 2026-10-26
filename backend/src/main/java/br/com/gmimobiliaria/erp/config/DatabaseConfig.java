package br.com.gmimobiliaria.erp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Usa a {@code DATABASE_URL} do sistema atual quando presente. Sem ela, vale a configuração padrão
 * {@code spring.datasource.*} (usada nos testes com banco em memória).
 */
@Configuration
@ConditionalOnProperty(name = "DATABASE_URL")
public class DatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(Environment environment, DataSourceProperties properties) {
        var url = DatabaseUrl.parse(environment.getRequiredProperty("DATABASE_URL"));
        properties.setUrl(url.jdbcUrl());
        properties.setUsername(url.username());
        properties.setPassword(url.password());
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
