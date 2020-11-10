package de.danielw

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.vault.annotation.VaultPropertySource

import static org.springframework.vault.annotation.VaultPropertySource.Renewal.OFF

@VaultPropertySource(
    value = ["secret/database/default"], // additional security context path besides path from bootstrap.yml
    renewal = OFF
)
@Configuration
@Slf4j
class SecretConfig {

  @Autowired
  Environment environment

  @Value('${db-user}')
  String dbUser

  @Value('${db-password}')
  String dbPassword

  @Bean
  DatabaseCredentials databaseCredentials() {
    log.info("Credentials via @VaultPropertySource and @Value: ${dbUser} | ${dbPassword}")
    log.info("Credentials via Environment: ${environment.getProperty("db-user")} | ${environment.getProperty("db-password")}")
    return new DatabaseCredentials(
        username: environment.getProperty("db-user"),
        password: environment.getProperty("db-password")
    )
  }
}
