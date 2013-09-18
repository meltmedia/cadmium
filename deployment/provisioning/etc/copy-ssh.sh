#!/usr/bin/env bash

sudo mkdir -p ~vagrant/bin
sudo cp ~vagrant/ssh/meltmedia-gene-deploy ~vagrant/bin/key
sudo cp ~vagrant/ssh/meltmedia-gene-deploy.pub ~vagrant/bin/key.pub 
sudo chmod 644 ~vagrant/bin/key
sudo chmod 644 ~vagrant/bin/key.pub