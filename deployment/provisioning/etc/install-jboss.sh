#!/usr/bin/env bash

set -e

if [[ "${1}###" == "###" ]]; then

  echo "Please specify a jboss to install"
  exit 1

fi

JBOSS_ZIP="/opt/${1}.zip"
JBOSS_ROOT_DIR=$(unzip -qql "${JBOSS_ZIP}" | head -n1 | tr -s ' ' | cut -d' ' -f5- | tr -d '/')

echo "Installing JBoss ${JBOSS_ZIP} into /opt/${JBOSS_ROOT_DIR}"

if [ -e "${JBOSS_ZIP}" ]; then
  if [ ! -e "/opt/${JBOSS_ROOT_DIR}" ]; then
    echo "Unzipping ${JBOSS_ZIP} to /opt/${JBOSS_ROOT_DIR}"
    pushd /opt
    sudo unzip -qq "${JBOSS_ZIP}"
    sudo ln -s ${JBOSS_ROOT_DIR} jboss
    popd 
  fi
fi

if ! (getent passwd jboss > /dev/null 2> /dev/null); then
  echo "Creating jboss user"
  sudo useradd -r -M -d "/opt/${JBOSS_ROOT_DIR}" jboss
fi

echo "Linking log directories"
if [ ! -e "/opt/${JBOSS_ROOT_DIR}/domain/log" ]; then
  sudo mkdir -p "/opt/${JBOSS_ROOT_DIR}/domain/log"
fi

if [ ! -e "/var/log/jboss-domain" ]; then
  sudo ln -s /opt/${JBOSS_ROOT_DIR}/domain/log /var/log/jboss-domain
fi

if [ ! -e "/opt/${JBOSS_ROOT_DIR}/domain/servers" ]; then
  sudo mkdir -p "/opt/${JBOSS_ROOT_DIR}/domain/servers"
fi

if [ ! -e "/var/log/jboss-servers" ]; then
  sudo ln -s /opt/${JBOSS_ROOT_DIR}/domain/servers /var/log/jboss-servers
fi

if [ ! -e "/opt/${JBOSS_ROOT_DIR}/standalone/log" ]; then
  sudo mkdir -p "/opt/${JBOSS_ROOT_DIR}/standalone/log"
fi

if [ ! -e "/var/log/jboss-standalone" ]; then
  sudo ln -s /opt/${JBOSS_ROOT_DIR}/standalone/log /var/log/jboss-standalone
fi

if [[ "${2}###" != "###" ]]; then
  . ${2}
fi

echo "Adding jboss admin user with password ${ADMIN_PASSWD}"
sudo /opt/${JBOSS_ROOT_DIR}/bin/add-user.sh --silent admin "${ADMIN_PASSWD}" 2> /dev/null

sudo chown -R jboss:jboss /opt/${JBOSS_ROOT_DIR}

set +e
