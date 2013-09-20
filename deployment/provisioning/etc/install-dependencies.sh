#!/usr/bin/env bash

#Update package cache
echo "Installing dependencies"
sudo apt-get update
sudo apt-get -y install openjdk-6-jdk unzip zip curl apache2 maven git