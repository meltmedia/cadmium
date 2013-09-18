#/usr/bin/env bash

if [ -e "/opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh" ]; then
  echo "Logging into jboss 7 to deploy a war: admin:${ADMIN_PASSWD}"
  /opt/${JBOSS_ROOT_DIR}/bin/jboss-cli.sh -c --user=admin --password=${ADMIN_PASSWD} --file=install-cadmium.jboss
fi