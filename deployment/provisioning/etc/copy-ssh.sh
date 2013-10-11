#!/usr/bin/env bash

sudo mkdir -p ~vagrant/bin
if [ -e ~vagrant/ssh/meltmedia-gene-deploy ]; then
  sudo cp ~vagrant/ssh/meltmedia-gene-deploy ~vagrant/bin/key
  sudo cp ~vagrant/ssh/meltmedia-gene-deploy.pub ~vagrant/bin/key.pub 
elif [ -e ~vagrant/ssh/id_rsa ]; then
  sudo cp ~vagrant/ssh/id_rsa ~vagrant/bin/key
  sudo cp ~vagrant/ssh/id_rsa.pub ~vagrant/bin/key.pub 
fi
sudo chmod 644 ~vagrant/bin/key
sudo chmod 644 ~vagrant/bin/key.pub