#!/usr/bin/env bash
if [ -z "$1" ]
then
    echo "Please supply a subdomain to create a certificate for";
    echo "e.g. www.mysite.com"
    exit;
fi

if [ ! -f rootCA.pem ]; then
    echo 'Please run "create_root_cert.sh" first, and try again!'
    exit;
fi
if [ ! -f v3.ext ]; then
    echo 'Please download the "v3.ext" file and try again!'
    exit;
fi

DOMAIN=$1
COMMON_NAME=$1
SUBJECT="/C=SE/ST=None/L=NB/O=None/CN=$COMMON_NAME"
NUM_OF_DAYS=999
openssl req -new -newkey rsa:2048 -sha256 -nodes -keyout server.key -subj "$SUBJECT" -out server.csr
cat v3.ext | sed s/%%DOMAIN%%/"$COMMON_NAME"/g > /tmp/__v3.ext

openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial -out server.crt -days $NUM_OF_DAYS -sha256 -extfile /tmp/__v3.ext

