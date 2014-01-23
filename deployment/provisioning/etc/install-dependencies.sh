#!/usr/bin/env bash

if [ -e packages-to-install ]; then
  PACKAGES=$(ls packages-to-install)
fi

#Update package cache
echo "Installing dependencies"
sudo apt-get update
sudo apt-get -y install openjdk-6-jdk unzip zip curl apache2 maven git $PACKAGES

git config --global user.name "${USERNAME}"
git config --global user.email "${USERNAME}.localhost"
