#!/bin/bash

echo "###########################################################################"
echo "# Downloading Vault 1.5.5                                                 #"
echo "###########################################################################"

VAULT_VER="1.5.5"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_ZIP="vault_${VAULT_VER}_${UNAME}_amd64.zip"

# cleanup
mkdir -p vault
mkdir -p download

if [[ ! -f "download/${VAULT_ZIP}" ]] ; then
    cd download
    echo "Downloading vault from https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}"
    curl "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}" --output "${VAULT_ZIP}"

    if [[ $? != 0 ]] ; then
      echo "Cannot download Vault"
      exit 1
    fi
    cd ..
fi

cd vault

if [[ -f vault ]] ; then
  rm vault
fi

unzip ../download/${VAULT_ZIP}
chmod a+x vault

# check
./vault --version
