#!/bin/bash

# This runs as root on the server

chef_binary=chef-solo

set -e

# Are we on a vanilla system?
if ! (type "$chef_binary" > /dev/null 2> /dev/null); then
	sudo apt-get install -y curl
    curl -L https://www.opscode.com/chef/install.sh | sudo bash
fi 
echo "Running chef-solo..."
"$chef_binary" -c solo.rb -j solo.json # -W -l debug