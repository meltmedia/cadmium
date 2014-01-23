#!/usr/bin/env bash

if [ -e "loggly.key" ]; then
  if [ ! -e "/opt/cadmium" ]; then
  	sudo mkdir -p /opt/cadmium
    sudo chown jboss:jboss /opt/cadmium
  fi

  if [ ! -e "/opt/cadmium/loggly.key" ]; then
  	sudo cp loggly.key /opt/cadmium/loggly.key
  	if (getent passwd jboss > /dev/null 2> /dev/null); then
  	  sudo chown jboss:jboss /opt/cadmium/loggly.key
  	fi
  fi

  if [ ! -e "/opt/cadmium/server-one" ]; then
    sudo mkdir -p /opt/cadmium/server-one
    sudo chown jboss:jboss /opt/cadmium/server-one
  fi

  if [ -e "/opt/cadmium/server-one" ]; then
  	if [ ! -e "/opt/cadmium/server-one/loggly.key" ]; then
  	  sudo cp loggly.key /opt/cadmium/server-one/loggly.key
  	  if (getent passwd jboss > /dev/null 2> /dev/null); then
  	    sudo chown jboss:jboss /opt/cadmium/server-one/loggly.key
  	  fi
  	fi
  fi
fi