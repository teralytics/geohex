#!/bin/bash

set -x

echo "Running travis_credentials script"

mkdir -p ~/.bintray
FILE=~/.bintray/.credentials
cat <<EOF >$FILE
realm = Bintray API Realm
host = api.bintray.com
user = $BINTRAY_USER
password = $BINTRAY_API_KEY
EOF

cat <<EOF >~/.npmrc
//registry.npmjs.org/:_authToken=$NPMJS_AUTH_TOKEN
EOF
