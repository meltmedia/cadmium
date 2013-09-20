#!/usr/bin/env bash

if !(type mvn > /dev/null 2> /dev/null); then
  sudo apt-get -y install maven
fi

if [ ! -e ~/.cadmium ]; then
  wget https://raw.github.com/meltmedia/cadmium/master/cli-install.py
  chmod +x cli-install.py
  ./cli-install.py
fi

if !(grep "/.cadmium/" .profile > /dev/null 2> /dev/null); then
  ( cat <<EOF
export PATH=$PATH:$HOME/.cadmium/bin
EOF
  ) >> .profile
fi