#!/usr/bin/env bash

if [ -e users ]; then
  if [ -e shiro.ini ]; then 
    echo "Creating global shiro users..."
    USERS_LIST="$(cat users)"
    cp shiro.ini shiro-updated.ini
    for A_USER in ${USERS_LIST}; do
      NEW_USERNAME=$(echo "${A_USER}" | cut -d'|' -f1)
      NEW_PASSWD=$(echo "${A_USER}" | cut -d'|' -f2)
      NEW_PASSWD=$(./shiro.sh "${NEW_PASSWD}")
      echo "Creating user ${NEW_USERNAME} with password ${NEW_PASSWD}"
      sed -i "/\[users\]/a \ \n${NEW_USERNAME}=${NEW_PASSWD}" shiro-updated.ini
      sudo cp shiro-updated.ini /opt/cadmium/shiro.ini
      sudo mv shiro-updated.ini /opt/cadmium/server-one/shiro.ini
      sudo chown jboss:jboss /opt/cadmium/shiro.ini
      sudo chown jboss:jboss /opt/cadmium/server-one/shiro.ini
    done
  fi
fi