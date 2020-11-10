#!/bin/bash

echo "###########################################################################"
echo "# Start Vault on http://localhost:8200                                    #"
echo "###########################################################################"

VAULT_BIN="vault/vault"

${VAULT_BIN} server \
            -config=vault.hcl \
            -dev \
            -dev-root-token-id="00000000-0000-0000-0000-000000000000" \


exit $?

