#!/usr/bin/env bash

echo "Initializing JBoss"

if [ ! -e "/etc/jboss-as/jboss-as.conf" ]; then
  echo "Configuring jboss service"
  sudo mkdir -p /etc/jboss-as
  ( cat <<EOF
JBOSS_HOME="/opt/${JBOSS_ROOT_DIR}"
JBOSS_USER=jboss
EOF
  ) > jboss-as.conf
  sudo mv jboss-as.conf /etc/jboss-as/jboss-as.conf
fi

if [ ! -e "/etc/init.d/jboss" ]; then
  if [ -e "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-domain.sh" ]; then
    sudo ln -s "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-domain.sh" /etc/init.d/jboss
    sudo sed -i "s_/etc/init.d/functions_/lib/lsb/init-functions_g" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-domain.sh"
    sudo sed -i "s_/bin/sh_/bin/bash_" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-domain.sh"
    sudo sed -i "s/success/log_success_msg Success/g" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-domain.sh"
    sudo sed -i "s/failure/log_failure_msg Failed/g" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-domain.sh"
  fi

  if [ -e "/etc/init.d/jboss" ]; then
    sudo update-rc.d -f jboss defaults
  fi
fi
if [ ! -e "/etc/init.d/jboss-as-standalone" ]; then
  if [ -e "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-standalone.sh" ]; then
    sudo ln -s "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-standalone.sh" /etc/init.d/jboss-as-standalone
    sudo sed -i "s_/etc/init.d/functions_/lib/lsb/init-functions_g" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-standalone.sh"
    sudo sed -i "s_/bin/sh_/bin/bash_" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-standalone.sh"
    sudo sed -i "s/success/log_success_msg Success/g" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-standalone.sh"
    sudo sed -i "s/failure/log_failure_msg Failed/g" "/opt/${JBOSS_ROOT_DIR}/bin/init.d/jboss-as-standalone.sh"
  fi

  if [ ! -e "/etc/init.d/jboss" ]; then
    if [ -e "/etc/init.d/jboss-as-standalone" ]; then
      sudo update-rc.d -f jboss-as-standalone defaults
    fi
  fi
fi

if !(grep "${HOSTNAME}" /etc/hosts > /dev/null 2> /dev/null); then
  sudo sed -i.bak "/localhost/ a\
127.0.0.1 ${HOSTNAME}" /etc/hosts
fi

