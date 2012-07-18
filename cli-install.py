#! /usr/bin/env python
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

import os, os.path, sys, stat, shutil, string

cadmium_version = '0.1.0-SNAPSHOT'

cadmium_sh = """#! /bin/sh
newest_jar=~/.cadmium/cadmium-cli.jar
if [ -d ~/.m2/repository/com/meltmedia/cadmium/cadmium-cli ]; then

  version_list=`ls ~/.m2/repository/com/meltmedia/cadmium/cadmium-cli`
  for version in $version_list; do

    if [ "$version" == "maven-metadata-local.xml" ]; then
      continue
    fi  
    current_file=~/.m2/repository/com/meltmedia/cadmium/cadmium-cli/$version/cadmium-cli-$version.jar
    if [ -s $current_file ]; then
      if [ $current_file -nt $newest_jar ]; then
        newest_jar=$current_file
      fi  
    fi  

  done

fi
java -jar $newest_jar "$@"
"""
user_dir = os.path.expanduser('~/.cadmium')

path_var = os.getenv('PATH')
path = string.split(path_var, os.pathsep)
maven_exists = False
maven_home = None
for item in path:
  if os.path.exists(item + '/mvn'):
    maven_home = item
    maven_exists = True
    break

if not maven_exists:
  print 'Please install maven before continuing'
  sys.exit(1)

if not os.path.exists(user_dir):
  os.mkdir(user_dir)

if not os.path.exists(user_dir + '/bin'):
  os.mkdir(user_dir + '/bin')

if not os.path.exists(user_dir + '/bin/cadmium'):
  fd = open(user_dir + '/bin/cadmium', 'w')
  try:
    fd.write(cadmium_sh)
    fd.flush()
  finally:
    fd.close()

mode = os.stat(user_dir + '/bin/cadmium').st_mode
if stat.S_IMODE(mode) != 0755:
  os.chmod(user_dir + '/bin/cadmium', 0755)

has_path = False
for item in path:
  if item == user_dir + '/bin/cadmium':
    has_path = True

if not has_path:
  fd = open(os.path.expanduser('~/.profile'), 'a')
  try:
    fd.write('\nexport PATH="$PATH:~/.cadmium/bin"\n')
    fd.flush()
    os.putenv('PATH', path_var + os.pathsep + '~/.cadmium/bin')
  finally:
    fd.close()

if os.path.exists(os.path.expanduser('~/.m2/repository/com/meltmedia/cadmium/cadmium-cli')):
  shutil.rmtree(os.path.expanduser('~/.m2/repository/com/meltmedia/cadmium/cadmium-cli'), True)

os.spawnl(os.P_WAIT, maven_home + os.sep + 'mvn', 'mvn', '-U', 'org.apache.maven.plugins:maven-dependency-plugin:2.4:get', '-Dartifact=com.meltmedia.cadmium:cadmium-cli:' + cadmium_version + ':jar', '-Ddest=' + os.path.expanduser('~/.cadmium/cadmium-cli.jar'), '-Dtransitive=false')

os.execl(user_dir + '/bin/cadmium', 'cadmium', 'check')
