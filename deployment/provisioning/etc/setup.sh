#!/usr/bin/env bash

JBOSS_DIST="jboss-as-7.1.1.Final"
JBOSS_URL="http://download.jboss.org/jbossas/7.1/${JBOSS_DIST}/${JBOSS_DIST}.zip"

if [[ "NOPARAM$1" != "NOPARAM" ]]; then
  JBOSS_URL="$1"
  JBOSS_DIST=${JBOSS_URL##*/}
  JBOSS_DIST=${JBOSS_DIST%%\.zip}
fi

set -e

cd $( dirname "${BASH_SOURCE[0]}" )

./install-dependencies.sh

./download-jboss.sh "${JBOSS_URL}"

./install-jboss.sh "${JBOSS_DIST}"

./setup-loggly.sh

./install-shiro.sh

./install-apache.sh

./install-keys.sh

set +e
echo "Done"