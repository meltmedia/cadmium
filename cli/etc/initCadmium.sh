#!/bin/sh

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
