#!/usr/bin/env bash

./bin/copy-ssh.sh

if [ -e bin/server.crt ] && [ -e bin/server.key ]; then
  sudo mkdir -p /etc/apache2-ssl
  sudo cp bin/server.* /etc/apache2-ssl
  sudo chmod 600 /etc/apache2-ssl/server.key
  sudo chown -R www-data /etc/apache2-ssl
  sed -i '.bak' \
      -e "s/X-Forwarded-Port  \"80\"/X-Forwarded-Port  \"8080\"/g" \
      -e "s/X-Forwarded-Port  \"443\"/X-Forwarded-Port  \"8443\"/g" \
      -e "s/^#//g" \
      -e "s_SSLCertificateChainFile /etc/apache2-ssl/CA.crt_#SSLCertificateChainFile /etc/apache2-ssl/CA.crt_g" \
      -e "s/star_cadmium_com/server/g" \
      bin/cadmium-apache2.cfg
fi

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
