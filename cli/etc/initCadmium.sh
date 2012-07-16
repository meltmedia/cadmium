#!/bin/sh
#
#    Copyright 2012 meltmedia
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#


if [ $UID -ne 0 ]; then
  echo "Please run this script as root"
  exit 1;
fi

CONTENT_DIR=/Library/WebServer/Cadmium
REPO=git://github.com/meltmedia/test-content-repo.git
REPO_DIR_NAME=git-checkout
RENDERED_DIR=renderedContent

mkdir $CONTENT_DIR
cd $CONTENT_DIR
git clone $REPO $REPO_DIR_NAME
cp -rf $REPO_DIR_NAME $RENDERED_DIR
rm -rf $RENDERED_DIR/.git
chmod -R u+rw .

if [ "X$1" != "X" ]; then
  chown -R $1 $CONTENT_DIR
fi
