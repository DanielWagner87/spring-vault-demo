package de.danielw

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.yml")
class VaultProperties {

//  @Value('${vault.token}')
//  String vaultToken
//
//  @Value('${vault.host}')
//  String vaultHost
//
//  @Value('${vault.port}')
//  int vaultPort

}
