#!/usr/bin/env bash

#Update package cache
sudo apt-get update

if ! type javac > /dev/null 2> /dev/null; then
  echo "Installing java..."

  #Install java
  sudo apt-get -y install openjdk-6-jdk
fi

if ! type unzip > /dev/null 2> /dev/null; then
  echo "Installing unzip..."

  #Install unzip
  sudo apt-get -y install unzip
fi

if ! type zip > /dev/null 2> /dev/null; then
  echo "Installing zip..."

  #Install zip
  sudo apt-get -y install zip
fi

if ! type curl > /dev/null 2> /dev/null; then
  echo "Installing curl..."

  #Install curl
  sudo apt-get -y install curl
fi