#!/usr/bin/env bash
echo "Setting up cadmium deployer config..."
if [ -e key.pub ]; then
  if [ ! -e ../.ssh/authorized_keys ]; then
    touch ../.ssh/authorized_keys
    chown $USERNAME ../.ssh/authorized_keys
  fi

  SSH_KEY=`cat key.pub`
  if !(grep "${SSH_KEY}" ../.ssh/authorized_keys > /dev/null 2> /dev/null); then
    echo ${SSH_KEY} >> ../.ssh/authorized_keys
  fi
fi

if !(type git > /dev/null 2> /dev/null); then
  sudo apt-get install -y git
  git config --global user.name "${USERNAME}"
  git config --global user.email "${USERNAME}.localhost"
fi

if [ ! -e ../deployer-config.git ]; then
  mkdir ../deployer-config.git
  pushd ../deployer-config.git
  git init --bare
  popd
  chown -R $USERNAME ../deployer-config.git
fi

if [ ! -e ../deployer-config ]; then
  pushd ..
  git clone deployer-config.git
  pushd deployer-config
  touch README.md
  git add README.md
  git commit -m "Initial commit"
  git push -uf origin master
  git checkout -b cfg-master
  ( cat <<EOF
default:
  jboss-api: !jboss-api
    username: admin
    password: ${ADMIN_PASSWD}
EOF
  ) > config.yml
  git add config.yml
  git commit -m "Added deployer configuration"
  git push -u origin cfg-master
  popd
  popd
  chown -R $USERNAME ../deployer-config*
fi

if [ -e cadmium-deployer.war ]; then

  pushd ..
  CADMIUM_BIN="$(pwd)/.cadmium/bin"
  export PATH="$PATH:${CADMIUM_BIN}"
  popd
  
  mv cadmium-deployer.war cadmium-deployer.war.bak
  cadmium init-war --existingWar cadmium-deployer.war.bak -C cfg-master -R $USER@$HOSTNAME:deployer-config.git cadmium-deployer.war

  chown $USERNAME cadmium-deployer.war*
fi