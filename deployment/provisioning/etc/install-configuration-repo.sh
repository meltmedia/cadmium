#!/usr/bin/env bash

if [ -e config ]; then
  CONFIG_DIR="$(pwd)/config"
  USER_HOME=$(eval echo ~${USERNAME})
  mkdir -p "${USER_HOME}/git-repo.git"
  
  pushd "${USER_HOME}/git-repo.git" > /dev/null 2> /dev/null
  
  git init --bare
  
  pushd .. > /dev/null 2> /dev/null
  
  git clone git-repo.git
  
  pushd git-repo > /dev/null 2> /dev/null

  touch README.md
  git add README.md
  git commit -m "Committed readme"
  git push -u origin master

  git checkout -b cfg-master
  cp -r ${CONFIG_DIR}/* .
  git add -A
  git commit -m "Initial configuration"
  git push -u origin cfg-master

  git checkout master
  git checkout -b cd-master
  git push -u origin cd-master

  popd > /dev/null 2> /dev/null

  sudo chown -R $USERNAME:$USERNAME git-repo git-repo.git

  popd > /dev/null 2> /dev/null
  popd > /dev/null 2> /dev/null
fi