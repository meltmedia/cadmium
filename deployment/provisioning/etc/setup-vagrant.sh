#!/usr/bin/env bash

./bin/copy-ssh.sh

if [ "X${SUDO_USER}" == "X" ]; then
  ./bin/setup.sh "$@"
else
  su -c "./bin/setup.sh $@" ${SUDO_USER}
fi