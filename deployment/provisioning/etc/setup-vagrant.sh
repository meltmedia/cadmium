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

if !(grep vagrant /etc/hosts > /dev/null 2> /dev/null); then
  sudo sed -i'' "/precise64/ a\
127.0.0.1 vagrant" /etc/hosts
fi
