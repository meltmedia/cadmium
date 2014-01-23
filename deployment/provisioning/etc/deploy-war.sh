#/usr/bin/env bash

set -e

if [ -e "/opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh" ]; then
  echo "Logging into jboss 7 to deploy a war: admin:${ADMIN_PASSWD}"
  if [ -e jboss-scripts ]; then
    SCRIPTS=$(ls jboss-scripts)
    for SCRIPT in $SCRIPTS; do
      echo "Running ${SCRIPT} in jboss."
      /opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh -c --user=admin --password=${ADMIN_PASSWD} --file=jboss-scripts/${SCRIPT}
    done
  else
    /opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh -c --user=admin --password=${ADMIN_PASSWD} --file=install-cadmium.jboss
  fi
fi