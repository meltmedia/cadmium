#!/usr/bin/env bash

APACHE2_MODS="headers proxy proxy_ajp proxy_balancer setenvif ssl"

function link_if_exists {
  TARGET=$1
  LINK=$2

  if [ -e "$TARGET" ]; then
    if [ ! -e "$LINK" ]; then
      sudo ln -s $TARGET $LINK
      return 0
    else
      return 1
    fi
  else
  	return 1
  fi
}

if !(type apache2ctl > /dev/null 2> /dev/null); then
  echo "Installing apache2";
  sudo apt-get install -y apache2
fi

if [ ! -e "/etc/apache2/sites-available/cadmium" ]; then
  echo "Adding cadmium configuration to apache."
  
  sudo cp cadmium-apache2.cfg /etc/apache2/sites-available/cadmium
fi

if [ ! -e "/etc/apache2/sites-enabled/000-cadmium" ]; then
  echo "Enabling cadmium site."

  sudo ln -s /etc/apache2/sites-available/cadmium /etc/apache2/sites-enabled/000-cadmium
fi

if [ -e "/etc/apache2/sites-enabled/000-default" ]; then
  sudo rm -f /etc/apache2/sites-enabled/000-default
fi

for MOD in ${APACHE2_MODS}; do
  link_if_exists "/etc/apache2/mods-available/${MOD}.load" "/etc/apache2/mods-enabled/${MOD}.load"
  LOADED=$?
  link_if_exists "/etc/apache2/mods-available/${MOD}.conf" "/etc/apache2/mods-enabled/${MOD}.conf"
  CONF=$?
  if [[ "$LOADED" == "0" || "$CONF" == "0" ]]; then
  	echo "Enabling module ${MOD}."
  fi
done
echo "Reloading apache2 configuraion"
sudo service apache2 reload