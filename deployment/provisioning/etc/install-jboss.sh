#!/usr/bin/env bash

ADMIN_PASSWD="p@\$\$w0rd"

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

sudo sed -i '/AJP\/1.3/d' "/opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml"
sudo sed -i '/AJP\/1.3/d' "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml"

if ! (grep "AJP/1.3" "/opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml" > /dev/null 2> /dev/null); then
  sudo sed -i '/jboss:domain:web:/a \
                 <connector name="ajp" protocol="AJP/1.3" scheme="http" socket-binding="ajp"/>' "/opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml"
fi

if (grep "127.0.0.1" "/opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml" > /dev/null 2> /dev/null); then
  sudo sed -i 's/127.0.0.1/0.0.0.0/g' "/opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml"
fi

if ! (grep "AJP/1.3" "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml" > /dev/null 2> /dev/null); then
  sudo sed -i '/jboss:domain:web:/a \
        <connector name="ajp" protocol="AJP/1.3" scheme="http" socket-binding="ajp"/>' "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml"
fi

if (grep "127.0.0.1" "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml" > /dev/null 2> /dev/null); then
  sudo sed -i 's/127.0.0.1/0.0.0.0/g' "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml"
fi

echo "Adding jboss admin user with password ${ADMIN_PASSWD}"
sudo /opt/${JBOSS_ROOT_DIR}/bin/add-user.sh --silent admin "${ADMIN_PASSWD}" 2> /dev/null

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

if (grep "resolve-parameter-values" "/opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.xml" > /dev/null 2> /dev/null); then
  sudo sed -i 's_<resolve-parameter-values>false</resolve-parameter-values>_<resolve-parameter-values>true</resolve-parameter-values>_g' "/opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.xml"
fi

set +e

sudo chown -R jboss:jboss /opt/${JBOSS_ROOT_DIR}
service jboss status > /dev/null 2> /dev/null
if [ "$?" -eq 3 ]; then
  sudo service jboss start
elif [ "$?" -eq 1 ]; then
  service jboss-as-standalone status > /dev/null 2> /dev/null
  if [ "$?" -eq 3 ]; then
    sudo service jboss-as-standalone start
  fi
fi

if [ ! -e "/opt/cadmium" ]; then
  sudo mkdir -p /opt/cadmium
fi 

if [ ! -e "/opt/cadmium/team.properties" ]; then
  sudo cp team.properties /opt/cadmium/team.properties
fi

if [ ! -e "/opt/cadmium/server-one/maven" ]; then
  sudo ln -s /opt/cadmium/maven /opt/cadmium/server-one/maven
fi

sudo chown -R jboss:jboss /opt/cadmium

# Configuring jboss
if [ -e "/opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh" ]; then
  /opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh -c --user=admin --password="${ADMIN_PASSWD}" --file=jboss-config.cli --properties=jboss.properties
  sleep 5
  /opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh -c --user=admin --password="${ADMIN_PASSWD}" --file=jboss-post.cli --properties=jboss.properties
elif [ -e "/opt/${JBOSS_ROOT_DIR}/bin/jboss-admin.sh" ]; then
  /opt/${JBOSS_ROOT_DIR}/bin/jboss-admin.sh -c --user=admin --password="${ADMIN_PASSWD}" --file=jboss-config.cli --properties=jboss.properties
  sleep 5
  /opt/${JBOSS_ROOT_DIR}/bin/jboss-admin.sh -c --user=admin --password="${ADMIN_PASSWD}" --file=jboss-post.cli --properties=jboss.properties
else
  echo "No admin cli script found."
fi
