#!/bin/bash

echo "###########################################################################"
echo "# Start Vault on http://localhost:8200                                    #"
echo "###########################################################################"

VAULT_BIN="vault/vault"

export VAULT_ADDR="https://localhost:8200"
export VAULT_SKIP_VERIFY=1
export VAULT_CACERT="/Users/daniel.wagner/Documents/Clones/spring-vault-demo/setup-https/config/cert/ca/certs/localhost.cert.pem"

${VAULT_BIN} server -config=config/vault.hcl

exit $?
