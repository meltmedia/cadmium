#!/usr/bin/env bash

#Update package cache
sudo apt-get update

if ! type javac > /dev/null 2> /dev/null; then
  echo "Installing java..."

  #Install java
  sudo apt-get -y install openjdk-6-jdk
fi
