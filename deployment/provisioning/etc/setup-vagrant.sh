#!/usr/bin/env bash

./bin/copy-ssh.sh

if [ "X${SUDO_USER}" == "X" ]; then
  ./bin/setup.sh "$@"
else
  cd ~${SUDO_USER}
  export HOME=$(pwd)
  sudo chown -R ${SUDO_USER}:${SUDO_USER} bin
  sudo -u ${SUDO_USER} ./bin/setup.sh "$@"
fi