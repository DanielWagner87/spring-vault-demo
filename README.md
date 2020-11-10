# Spring Cloud Vault

## Local Setup

Für die Spring Boot Demo Applikation wird ein lokaler Vault benötigt. Für die Installation und Konfiguration sind bereits entsprechende Skripte vorhanden, die den Vault vorbereiten.

Dazu ist in einem Terminal zunächst folgendes Skript auszuführen:

```bash
setup/setup.sh
```

Dies lädt das Vault Binary herunter und startet den Vault Server im Dev Mode. Dazu wird die Config-Datei `setup/vault.hcl` benutzt.

Zum Anlegen der Demo Secrets und Policies ist folgendes Skript auszuführen:

```bash
setup/sample_data.sh
```

## Authentication

### Token

Wird der Server im Dev Mode gestartet, so ist das Root Token per Default `00000000-0000-0000-0000-000000000000`.

Für die token-basierte Authentifizierung ist in der `bootstrap.yml` lediglich folgende Konfiguration zu hinterlegen:

```yaml
spring:
  cloud:
    vault:
      uri: "http://localhost:8200"
      token: "00000000-0000-0000-0000-000000000000"
```

### AppRole

Neben der token-basierten Authentifizierung ist auch die Authentifizierung via AppRole möglich. Eine Role ist mit einer Policy verknüpft, welche Berechtigungen definiert.

Zu einer Role kann auch wieder ein Token erzeugt werden, welches für die token-basierte Authentifizierung verwendet werden kann.

Soll sich die Spring Boot Anwendung via AppRole authentifizieren, ist in der `bootstrap.yml` Folgendes zu hinterlegen:

```yaml
spring:
  cloud:
    vault:
      uri: "http://localhost:8200"
      authentication: APPROLE
      app-role:
        role: spring-role
        role-id: a5be238c-a3a5-372a-1c8a-59173cbb1634
        secret-id: c63f1412-7090-c008-4802-a7c99c2c0bc8
```

## Secrets lesen

Per Default werden die Secrets aus folgendem Secret Context Path geladen:

```default
/secret/{application name}/{profile}
/secret/{application name}
/secret/{default context}/{profile}
/secret/{default context}
```

Spring Boot loggt die Pfade beim Application Start.

### Via @Value

Dazu ist in der `bootstrap.yml` der Default Context zu hinterlegen. Per Default gilt der Wert, der für die Property `spring.application.name` gesetzt wird.

Es empfiehlt sich den Kontext explizit über die Property `spring.cloud.vault.kv.default-context` zu setzen.

Die Screts können anschließend via `@Value` injected werden:

```groovy
@Configuration
class Secrets {

  @Value('${username}')
  String username

  @Value('${password}')
  String password
}
```

Das Profil wird automatisch im Secret Context Path berücksichtigt. So ist bspw. die folgende Konfiguration ausreichend, um in Abhängigkeit des gewählten Profils
die entsprechenden Secrets aus dem Secret Context Path zu laden:

```yaml
spring:
  cloud:
    vault:
      uri: "http://localhost:8200"
      token: "my-token"
      kv:
        enabled: true
        default-context: "app-demo"
```

Unter der Annahme, dass für die Applikation die Profile `dev` und `prod` verwendet werden, ist die Struktur in Vault wie folgt:

```default
secrets
    app-demo
        dev
            username
            password
        prod
            username
            password
```

Sind die Secret Context Paths nicht mit den Profilen der Applikation aligned, ist auch folgende Konfiguration in der `bootstrap.yml` möglich:

```yaml
spring:
  cloud:
    vault:
      uri: "http://localhost:8200"
      token: "my-token"
      kv:
        enabled: true
        default-context: "app-demo/dev-stage"

---
spring:
  profiles: "prod"
  cloud:
    vault:
      kv:
        default-context: "app-demo/prod-stage"

```

### Via @VaultPropertySource

Mit `@VaultPropertySource` gibt es auch eine Variante, die von Springs `@PropertySource` Mechanismus Gebrauch macht.

```groovy
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
```

Möchte man dies auch stageabhängig machen, wird die Konfiguration etwas umfangreicher:

```groovy
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
```

### Via VaultOperations

Mit den `VaultOperations` bzw. der Implementierung `VaultTemplate` steht eine Klasse zur Verfügung, die das Auslesen eines Secrets wie folgt ermöglicht:

```groovy
String stage = environment.activeProfiles.contains("prod") ? "prod" : "dev"
VaultResponse response = vaultOperations.opsForKeyValue("secret", KV_2).get("app-demo-1/${stage}")
log.info("Username via VaultOperations: ${response.data["username"]}")
log.info("Password via VaultOperations: ${response.data["password"]}")
```

## Secrets schreiben

Beim Schreiben von Secrets ist darauf zu achten, dass bereits existierende Secrets im Pfad gelöscht werden und nur die Secrets am Ende in Vault stehen,
die via `put` übermittelt werden. Es ist daher ratsam, neue Secrets immer in einen gänzlich neuen Path zu schreiben.

```groovy
vaultOperations.opsForKeyValue("secret", KV_2).put("app-demo-1/test", [
    username: "test-username",
        password: "test-password"
])
```

## Secrets auflisten

Mit der `list` Operation können die Namen einzelner Ordner für einen konkreten Path ausgelesen werden. Es scheint nicht möglich zu sein, die Secret-Namen
aus einem konkreten Path zu lesen.

```groovy
List<String> secretsLvl1 = vaultOperations.opsForKeyValue("secret", KV_2).list("app-demo-1")
List<String> secretsLvl2 = vaultOperations.opsForKeyValue("secret", KV_2).list("app-demo-1/dev")
log.info("Values in lvl1: ${secretsLvl1.toString()}")
log.info("Values in lvl2: ${secretsLvl2.toString()}") // returns empty list
```

## Secrets löschen

Löscht alle Secrets am angegebenen Pfad:

```groovy
vaultOperations.opsForKeyValue("secret", KV_2).delete("app-demo-1/test")
```

Es scheint nicht möglich zu sein, ein einzelnes Secret innerhalb eines angegebenen Paths zu löschen.
