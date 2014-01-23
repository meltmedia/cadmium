#!/usr/bin/env bash

if [ -e modules ]; then
  MODULE_LIST=$(find modules -name "*.tar.gz")
  SCRIPT_DIR=$(pwd)
  pushd /opt/jboss/modules > /dev/null 2> /dev/null
  for MODULE in $MODULE_LIST; do
  	echo "Installing module $MODULE"
    sudo tar xzf ${SCRIPT_DIR}/$MODULE
  done
  sudo chown -R jboss:jboss .
  popd > /dev/null 2> /dev/null
fi