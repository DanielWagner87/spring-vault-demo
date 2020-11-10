echo "###########################################################################"
echo "# Putting sample data                                                     #"
echo "###########################################################################"

export VAULT_ADDR="http://localhost:8200"
export VAULT_TOKEN=00000000-0000-0000-0000-000000000000

echo "Creating spring policy"
vault auth enable approle
vault policy delete spring-policy
vault policy write spring-policy spring-policy.hcl
vault write auth/approle/role/spring-role \
    secret_id_ttl=240m \
    token_num_uses=10 \
    token_ttl=20m \
    token_max_ttl=30m \
    secret_id_num_uses=40 \
    token_policies=spring-policy

echo "Creating sample secrets"
vault kv put secret/app-demo-1/dev username=dev-username password=dev-password
vault kv put secret/app-demo-1/prod username=prod-username password=prod-password

vault kv put secret/app-demo-2/dev-stage username=dev-stage-username password=dev-stage-password
vault kv put secret/app-demo-2/prod-stage username=prod-stage-username password=prod-stage-password

vault kv put secret/database/default db-user=default-database-username db-password=default-database-password
vault kv put secret/database/dev db-user=dev-database-username db-password=dev-database-password
vault kv put secret/database/prod db-user=prod-database-username db-password=prod-database-password

echo "############################################"
echo "Role ID: $(vault read -field=role_id auth/approle/role/spring-role/role-id)"
echo "Secret ID: $(vault write -f -field=secret_id auth/approle/role/spring-role/secret-id)"
echo "############################################"
