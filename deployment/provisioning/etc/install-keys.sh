#!/usr/bin/env bash

if [ -e key ] && [ -e key.pub ]; then
  echo "Installing ssh key for github access."

  if [ ! -e /opt/cadmium/ssh ]; then
    sudo mkdir -p /opt/cadmium/ssh
  fi

  sudo cp key /opt/cadmium/ssh/meltmedia_deploy
  sudo cp key.pub /opt/cadmium/ssh/meltmedia_deploy.pub
  sudo chmod 600 /opt/cadmium/ssh/meltmedia_deploy
  sudo chown jboss:jboss /opt/cadmium/ssh
fi