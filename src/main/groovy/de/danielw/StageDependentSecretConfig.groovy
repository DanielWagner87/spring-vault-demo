package de.danielw

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.vault.annotation.VaultPropertySource
import org.springframework.vault.annotation.VaultPropertySources

import static org.springframework.vault.annotation.VaultPropertySource.Renewal.OFF

@VaultPropertySources([
    @VaultPropertySource(
        value = ["secret/database/dev"],
        renewal = OFF,
        propertyNamePrefix = "dev."
    ),
    @VaultPropertySource(
        value = ["secret/database/prod"],
        renewal = OFF,
        propertyNamePrefix = "prod."
    )
])
@Configuration
@Slf4j
class StageDependentSecretConfig {

  @Autowired
  Environment environment

  @Value('${dev.db-user}')
  String devDbUser

  @Value('${dev.db-password}')
  String devDbPassword

  @Value('${prod.db-user}')
  String prodDbUser

  @Value('${prod.db-password}')
  String prodDbPassword

  @Bean
  DatabaseCredentials databaseCredentials2() {
    log.info("[DEV] Credentials via @VaultPropertySource and @Value: ${devDbUser} | ${devDbPassword}")
    log.info("[PROD] Credentials via @VaultPropertySource and @Value: ${prodDbUser} | ${prodDbPassword}")

    if ("dev" in environment.activeProfiles) {
      return new DatabaseCredentials(
          username: devDbUser,
          password: devDbPassword
      )
    }
    return new DatabaseCredentials(
        username: prodDbUser,
        password: prodDbPassword
    )
  }
}
