#!/usr/bin/env bash

service jboss status > /dev/null 2> /dev/null
if [ "$?" -eq 3 ]; then
  sudo service jboss start
elif [ "$?" -eq 1 ]; then
  service jboss-as-standalone status > /dev/null 2> /dev/null
  if [ "$?" -eq 3 ]; then
    sudo service jboss-as-standalone start
  fi
fi