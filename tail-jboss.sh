#!/bin/bash
#
# This script requires the following scripts to run.
#
# remote-tail
# vagrant-tail
#
# both are located in the following git repo:
#
# git@github.com:jwm123/scripts.git
#


vagrant-tail /var/log/jboss-servers/server-one/log/server.log /var/log/jboss-servers/server-one/log/cadmium.localhost/cadmium.log /var/log/jboss-servers/server-one/log/test.cadmium.localhost/cadmium.log"$@"
