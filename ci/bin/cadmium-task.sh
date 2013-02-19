#!/bin/bash

PATH=$PATH:/home/bamboo/.cadmium/bin:/home/bamboo/bin

if [ -r "./Cakefile" ] && [ -r "docpad.coffee" ]; then
  npm install
  if [[ "$?" -eq "0" ]]; then
    ./node_modules/coffee-script/bin/cake deploy
    if [[ "$?" -eq "0" ]]; then
      exit 0
    else
      echo "Failed to deploy to dev."
      exit 1
    fi
  else
    echo "Failed to install project in node."
    exit 1
  fi
else
  echo "This is not a docpad app"
fi
