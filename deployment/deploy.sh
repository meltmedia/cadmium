#!/usr/bin/env bash

function print_usage {
  echo "USAGE: deploy.sh url [identity]"
  exit $1
}

function read_var {
  echo -n "$1"
  read READ_VAR
  if [ ${#READ_VAR} -eq 0 ]; then
    READ_VAR=$2
  fi
}

if [ "X${SSH_USER}" == "X" ]; then
  SSH_USER=ubuntu
fi

if [ "X$1" == "X" ]; then
  print_usage 1
fi

URL=$1

if (grep "$URL" ~/.ssh/known_hosts > /dev/null 2> /dev/null); then
  sed -i.bak "/^$URL/d" ~/.ssh/known_hosts
fi

SSH_KEY=-1

if [ "X$2" != "X" ]; then
  if [ -e "$2" ]; then
    SSH_KEY=$2
  else
    echo "SSH identity does not exist!"
  fi
fi

cd $( dirname "${BASH_SOURCE[0]}" )

SCRIPT_DIR=$(pwd)
if [[ "$SSH_KEY" != "-1" ]]; then
  SSH_OPT="-i ${SSH_KEY} ${SSH_OPT}"
fi

SSH_CMD="ssh ${SSH_OPT}"
SCP_CMD="scp ${SSH_OPT}"
MVN_REPO="http://repo1.maven.org/maven2/"
if [[ "X${DEFAULT_MVN_REPO}" != "X" ]]; then
  MVN_REPO="${DEFAULT_MVN_REPO}"
fi

TEAM_IDS="42646"
if [[ "X${DEFAULT_TEAM_IDS}" != "X" ]]; then
  TEAM_IDS="${DEFAULT_TEAM_IDS}"
fi

JBOSS_URL=
if [[ "X${DEFAULT_JBOSS_URL}" != "X" ]]; then
  JBOSS_URL="${DEFAULT_JBOSS_URL}"
fi

if (mvn clean install); then

  if [ -e target ]; then
    rm -rf target
  fi
  mkdir target
  set -e
  read_var "Environment [cadmium-local]: " "cadmium-local"
  ENVIRONMENT=${READ_VAR}
  read_var "Minimum Java Heap [128m]: " "128m"
  HEAP_MIN=${READ_VAR}
  read_var "Maximum Java Heap [256m]: " "256m"
  HEAP_MAX=${READ_VAR}
  read_var "Maximum Permgen Mem [256m]: " "256m"
  PERMGEN=${READ_VAR}
  read_var "Maven Repo Url [${MVN_REPO}]: " "${MVN_REPO}"
  MVN_REPO=${READ_VAR}

  ( cat <<EOF
ENVIRONMENT_NAME=${ENVIRONMENT}
MIN_HEAP_SIZE=${HEAP_MIN}
MAX_HEAP=${HEAP_MAX}
PERM_SIZE=${PERMGEN}
MAVEN_REPOSITORY=${MVN_REPO}
EOF
  ) > target/jboss.properties
  
  read_var "Team IDs [${TEAM_IDS}]: " "${TEAM_IDS}"
  TEAM_IDS=${READ_VAR}

  ( cat <<EOF
default=${TEAM_IDS}
EOF
  ) > target/teams.properties

  read_var "Loggly Key []: "
  LOGGLY_KEY=${READ_VAR}
  if [[ ${#LOGGLY_KEY} -ne 0 ]]; then 
    echo ${LOGGLY_KEY} > target/loggly.key
  fi

  echo -n "Generate Self-signed Cert: (y/[n]) "
  read GEN_CERT

  shopt -s nocasematch

  if [ "${GEN_CERT}" == "y" ]; then
    pushd target

    read -s -p "Enter passphrase: " PASSPHRASE
    echo ""
    echo "Generating key..."
    openssl genrsa -des3 -out server.key -passout pass:$PASSPHRASE 2048
    echo "Create CSR..."
    openssl req -new -key server.key -out server.csr -passin pass:$PASSPHRASE -subj "/CN=$URL"
    cp server.key server.key.org
    echo "Remove password from key..."
    openssl rsa -in server.key.org -out server.key -passin pass:$PASSPHRASE
    echo "Generating certificate..."
    openssl x509 -req -days 1000 -in server.csr -signkey server.key -out server.crt
 
    cp ../provisioning/src/main/filtered-resources/cadmium-apache2.cfg cadmium-apache2.cfg
    sed -i '' "s/^#//g" cadmium-apache2.cfg
    sed -i '' "s_SSLCertificateChainFile /etc/apache2-ssl/CA.crt_#SSLCertificateChainFile /etc/apache2-ssl/CA.crt_g" cadmium-apache2.cfg
    sed -i '' "s/star_cadmium_com/server/g" cadmium-apache2.cfg

    popd
  fi

  shopt -u nocasematch

  $SCP_CMD provisioning/target/cadmium-installer.tar.gz ${SSH_USER}@$URL:

  $SSH_CMD ${SSH_USER}@$URL "tar xzf cadmium-installer.tar.gz"

  if [ -e ~/.ssh/meltmedia_deploy ] && [ -e ~/.ssh/meltmedia_deploy.pub ]; then
    cp ~/.ssh/meltmedia_deploy target/key
    cp ~/.ssh/meltmedia_deploy.pub target/key.pub
  fi

  $SCP_CMD target/* ${SSH_USER}@$URL:bin/

  if [ -e target/server.crt ]; then
    echo "Installing apache cert"
    $SSH_CMD ${SSH_USER}@$URL <<EOF
      cd bin
      sudo mkdir -p /etc/apache2-ssl
      sudo cp server.* /etc/apache2-ssl
      sudo chmod 600 /etc/apache2-ssl/server.key*
      sudo chown -R www-data /etc/apache2-ssl
EOF
  fi

  $SSH_CMD -t ${SSH_USER}@$URL "HOSTNAME=$URL USERNAME=${SSH_USER} bin/setup.sh ${JBOSS_URL}"
else
  echo "Failed to build deployment assembly"
  exit 1
fi
