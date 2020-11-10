package de.danielw

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.env.Environment
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.VaultResponse

import static org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend.KV_2

@SpringBootApplication
@Slf4j
class SpringVaultDemoApplication implements CommandLineRunner {

	@Value('${username}')
	String username

	@Value('${password}')
	String password

	@Autowired
	VaultOperations vaultOperations

	@Autowired
	Environment environment

	static void main(String[] args) {
		SpringApplication.run(SpringVaultDemoApplication, args)
	}

	@Override
	void run(String... args) throws Exception {
		readSecret()
		listSecrets()
		// writeSecret()
		// deleteSecret()
	}

	private void readSecret() {
		log.info("Credentials via @Value: ${username} | ${password}")
		String stage = environment.activeProfiles.contains("prod") ? "prod" : "dev"
		VaultResponse response = vaultOperations.opsForKeyValue("secret", KV_2).get("app-demo-1/${stage}")
		log.info("Username via VaultOperations: ${response.data["username"]}")
		log.info("Password via VaultOperations: ${response.data["password"]}")
	}

	private void listSecrets() {
		List<String> secretsLvl1 = vaultOperations.opsForKeyValue("secret", KV_2).list("app-demo-1")
		List<String> secretsLvl2 = vaultOperations.opsForKeyValue("secret", KV_2).list("app-demo-1/dev")
		log.info("Values in lvl1: ${secretsLvl1.toString()}")
		log.info("Values in lvl2: ${secretsLvl2.toString()}")
	}

	private void writeSecret() {
		vaultOperations.opsForKeyValue("secret", KV_2).put("app-demo-1/test", [
		    username: "test-username",
				password: "test-password",
				test: "delete-me"
		])
	}

	private void deleteSecret() {
		vaultOperations.opsForKeyValue("secret", KV_2).delete("app-demo-1/test")
	}
}
