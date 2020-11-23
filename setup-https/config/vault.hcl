// Enable UI
ui = true

// Filesystem storage
storage "file" {
  path = "./vault-volume"
}

// TCP Listener
listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "config/cert/server.crt"
  tls_key_file = "config/cert/server.key"
  tls_min_version = "tls10"
  tls_disable_client_certs = "true"
}

disable_mlock = true
