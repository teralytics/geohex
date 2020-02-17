#!/bin/bash

cat <<EOF >~/.npmrc
//registry.npmjs.org/:_authToken=$NPMJS_AUTH_TOKEN
EOF
