package br.com.gmimobiliaria.erp;

import org.springframework.boot.SpringApplication;

/**
 * Sobe o backend com banco em memória e dados fictícios, sem tocar no Supabase.
 * Uso: {@code ./mvnw spring-boot:test-run -Dspring-boot.run.mainClass=br.com.gmimobiliaria.erp.LocalErpApplication} e, no frontend, {@code npm start}.
 * Login: admin@ficticio.local / Senha@123 (ou operacional@ficticio.local).
 */
public class LocalErpApplication {
    public static void main(String[] args) {
        SpringApplication.from(ErpApplication::main)
                .withAdditionalProfiles("test", "local")
                .run(args);
    }
}
