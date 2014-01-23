#!/usr/bin/env bash

JBOSS_URL="$1"
JBOSS_FILENAME="${JBOSS_URL##*/}"
JBOSS_FILENAME="${JBOSS_FILENAME%%\?*}"

if [ ! -e "/opt/${JBOSS_FILENAME}" ] ; then
  echo "Downloading ${JBOSS_FILENAME}..."
  if type curl > /dev/null 2> /dev/null ; then
    curl "${JBOSS_URL}" --compressed -s -O
    sudo mv ${JBOSS_FILENAME} /opt/${JBOSS_FILENAME}
  elif type wget > /dev/null 2> /dev/null ; then
    wget "${JBOSS_URL}" -q
    sudo mv ${JBOSS_FILENAME} /opt
  else
    echo "Cannot download ${JBOSS_FILENAME}!"
    exit 1
  fi
fi