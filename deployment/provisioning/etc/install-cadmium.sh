#!/usr/bin/env bash

echo "Installing cadmium..."

source jboss.properties

if [ ! -e "/opt/cadmium" ]; then
  sudo mkdir -p /opt/cadmium
fi 

if [ ! -e "/opt/cadmium/teams.properties" ]; then
  sudo cp teams.properties /opt/cadmium/teams.properties
fi

if [ ! -e "/opt/cadmium/server-one/maven" ] && [ -e "/opt/cadmium/maven" ] ; then
  if [ ! -e "/opt/cadmium/server-one" ]; then
    sudo mkdir -p /opt/cadmium/server-one
  fi
  sudo ln -s /opt/cadmium/maven /opt/cadmium/server-one/maven
fi

sudo chown -R jboss:jboss /opt/cadmium

if [ -e "/opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml" ]; then
  if [ -e "/opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml.bak" ]; then
  	sudo cp /opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml.bak /opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml
  fi
  if [ -e "cadmium-props.sed.bak" ]; then
  	cp cadmium-props.sed.bak cadmium-props.sed
  fi
  if [ -e "cadmium-domain-group-props.xml.bak" ]; then
  	cp cadmium-domain-group-props.xml.bak cadmium-domain-group-props.xml
  fi
  sed -i.bak -e "s/MIN_HEAP_SIZE/${MIN_HEAP_SIZE}/g" -e "s/MAX_HEAP/${MAX_HEAP}/g" -e "s/PERM_SIZE/${PERM_SIZE}/g" cadmium-props.sed
  sed -i.bak -e "s/ENVIRONMENT_NAME/${ENVIRONMENT_NAME}/g" -e "s#MAVEN_REPOSITORY#${MAVEN_REPOSITORY}#g" cadmium-domain-group-props.xml
  sudo sed -i.bak -f cadmium-props.sed /opt/${JBOSS_ROOT_DIR}/domain/configuration/domain.xml
fi

if [ -e "/opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml" ]; then
  if [ -e "/opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml.bak" ]; then
  	sudo cp /opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml.bak /opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml
  fi
  sudo sed -i.bak -f cadmium-props-host.sed /opt/${JBOSS_ROOT_DIR}/domain/configuration/host.xml
fi

if [ -e "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml" ]; then
  if [ -e "/opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml.bak" ]; then
  	sudo cp /opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml.bak /opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml
  fi
  if [ -e "cadmium-standalone-props.xml.bak" ]; then
  	cp cadmium-standalone-props.xml.bak cadmium-standalone-props.xml
  fi
  sed -i.bak -e "s/ENVIRONMENT_NAME/${ENVIRONMENT_NAME}/g" -e "s#MAVEN_REPOSITORY#${MAVEN_REPOSITORY}#g" cadmium-standalone-props.xml
  sudo sed -i.bak -f cadmium-props-standalone.sed /opt/${JBOSS_ROOT_DIR}/standalone/configuration/standalone.xml
fi