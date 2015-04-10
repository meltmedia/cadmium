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

import os, os.path, sys, stat, shutil, string, urllib, xml.etree.ElementTree, subprocess
from termios import tcflush, TCIOFLUSH

cadmium_sh = """#! /bin/bash
newest_jar=~/.cadmium/cadmium-cli.jar
if [ -d ~/.m2/repository/com/meltmedia/cadmium/cadmium-cli ]; then

  version_list=`ls ~/.m2/repository/com/meltmedia/cadmium/cadmium-cli`
  for version in $version_list; do

    if [[ "$version" == "maven-metadata-local.xml" ]]; then
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

cadmium_commit = """#! /usr/bin/env python
import os
import sys
import subprocess
import shutil
import re
import argparse

arg_parser = argparse.ArgumentParser()
arg_parser.add_argument("message", help="Message for deployment history")
arg_parser.add_argument("url", help="URL to deploy to")
arg_parser.add_argument("-d", "--directory", help="Overrides the directory to deploy [Default out]")
arg_parser.add_argument("-i", "--no_generate", help="Don't generate content just deploy", action="store_true")

args = arg_parser.parse_args()

if not os.path.exists(os.path.expanduser('~/.cadmium/bin/cadmium')):
  print "Please run `cli-install.py` before running `cake deploy`"
  sys.exit(1)

out_dir='out'
if args.directory:
  out_dir=args.directory

if args.no_generate and not os.path.exists(out_dir):
  print "No directory named [" + out_dir + "] exits! Please create one."
  sys.exit(1)

bamboo_build = False
return_code = subprocess.call(['git', 'fetch'])
if return_code == 128:
  bamboo_build = True

if not bamboo_build:
  git_status = subprocess.check_output(['git', 'status', '--short', '--branch'])
  status_lines = re.split('\\n', git_status)
  num_lines = len(status_lines) - 1
  if num_lines > 0 and re.search("^##", status_lines[0]):
    num_lines = num_lines - 1

  for x in status_lines[:]:
    if re.search(r"^\\?", x) or re.search(r"^\\!", x):
      num_lines = num_lines - 1

  pattern = re.compile(r"^##.*\\[((?:(?:ahead)|(?:behind)) \\d+)\\]$")
  if pattern.search(status_lines[0]):
    print "Your local is out of sync with origin: [" + pattern.match(status_lines[0]).group(1) + "]"
    sys.exit(1)

  if num_lines > 0:
    print "Please commit and push your changes."
    sys.exit(1)

  print "Attempting to update cadmium dependencies. If an update is available this may take a while."

  subprocess.call(['mvn', '-q', 'org.apache.maven.plugins:maven-dependency-plugin:2.4:get',
                   '-Dartifact=com.meltmedia.cadmium:cadmium-cli:LATEST:jar',
                   '-Ddest=' + os.path.expanduser('~/.cadmium/cadmium-cli.jar'), '-Dtransitive=false'])

message = args.message
url = args.url

try:
  source_repo_url = "bamboo"
  if not bamboo_build:
    source_repo_url = subprocess.check_output(['git', 'config', '--get', 'remote.origin.url']).strip()
  source_sha = subprocess.check_output(['git', 'rev-parse', 'HEAD']).strip()
  source_branch = subprocess.check_output(['git', 'symbolic-ref', 'HEAD']).strip().split('/')
  if len(source_branch) > 1:
    source_branch = source_branch[len(source_branch) - 1]

  source = "{\\"repo\\":\\""+source_repo_url+"\\",\\"sha\\":\\""+source_sha+"\\",\\"branch\\":\\""+source_branch+"\\"}"

  if not args.no_generate:
    if os.path.exists(out_dir):
      shutil.move(out_dir, out_dir+'-old')
    status = subprocess.call(['./node_modules/.bin/docpad', 'generate', '--env=production'])
    if status != 0:
      sys.exit(1)

  if not os.path.exists(out_dir+'/META-INF'):
    os.mkdir(out_dir+'/META-INF')

  if os.path.exists(out_dir+'/META-INF/source'):
    os.remove(out_dir+'/META-INF/source')

  fd = open(out_dir+'/META-INF/source', 'w')
  try:
    fd.write(source)
    fd.flush()
  finally:
    fd.close()

  status = subprocess.call(['cadmium', 'validate', out_dir])
  if status != 0:
    sys.exit(1)

  status = subprocess.call(['cadmium', 'commit', '--quiet-auth', '-m', '"'+message+'"', out_dir, url])
  if status != 0:
    sys.exit(1)

except subprocess.CalledProcessError:
  print "Please run this command from within a git repository."
finally:
  if not args.no_generate:
    if os.path.exists(out_dir+'-old'):
      if os.path.exists(out_dir):
        shutil.rmtree(out_dir)
      shutil.move(out_dir+'-old', out_dir)
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

if os.path.exists(user_dir + '/bin/cadmium'):
  os.remove(user_dir + '/bin/cadmium')
  
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

if os.path.exists(user_dir + '/bin/cadmium-commit'):
  os.remove(user_dir + '/bin/cadmium-commit')
  
if not os.path.exists(user_dir + '/bin/cadmium-commit'):
  fd = open(user_dir + '/bin/cadmium-commit', 'w')
  try:
    fd.write(cadmium_commit)
    fd.flush()
  finally:
    fd.close()

mode = os.stat(user_dir + '/bin/cadmium-commit').st_mode
if stat.S_IMODE(mode) != 0755:
  os.chmod(user_dir + '/bin/cadmium-commit', 0755)

if sys.argv[0][0] == '.' and os.path.exists(user_dir + '/bin/cli-install.py') and (not os.path.samefile(sys.argv[0], user_dir + '/bin/cli-install.py')):
  os.remove(user_dir + '/bin/cli-install.py')
  
if not os.path.exists(user_dir + '/bin/cli-install.py'):
  shutil.copy(sys.argv[0], user_dir + '/bin/cli-install.py')
  os.chmod(user_dir + '/bin/cli-install.py', 0755)

exitCode = subprocess.call(['curl', 'https://raw2.github.com/meltmedia/cadmium/master/vagrant-cadmium-up', '-o', user_dir + '/bin/vagrant-cadmium-up'])
if exitCode != 0:
  print 'Failed to download vagrant-cadmium-up!'
else:
  os.chmod(user_dir + '/bin/vagrant-cadmium-up', 0755)

has_path = False
for item in path:
  if item == (user_dir + '/bin'):
    has_path = True
    break

if not has_path:
  shell_run_commands = {
    '/bin/bash': '~/.profile',
    '/bin/zsh': '~/.zshrc'
  }
  shell = os.environ["SHELL"]
  rc_file = shell_run_commands.get(shell)
  if rc_file is None:
    sys.exit(' '.join(['Unknown shell', shell]))
  print 'Please run `. {}` before continuing!'.format(rc_file)
  has_path = (subprocess.call(['grep', '--silent', 'cadmium', os.path.expanduser(rc_file)]) == 0)
  if not has_path:
    fd = open(os.path.expanduser(rc_file), 'a')
    try:
      fd.write('\nexport PATH="$PATH:'+user_dir+'/bin"\n')
      fd.flush()
      os.putenv('PATH', path_var + os.pathsep + user_dir + '/bin')
    finally:
      fd.close()

print 'Downloading latest version of cadmium...'
subprocess.call(['mvn', '-q', 'org.apache.maven.plugins:maven-dependency-plugin:2.4:get', '-Dartifact=com.meltmedia.cadmium:cadmium-cli:LATEST:jar', '-Ddest=' + os.path.expanduser('~/.cadmium/cadmium-cli.jar'), '-Dtransitive=false'])

exitCode = 1;
tries = 2;
while exitCode == 1 and tries > 0:
  sys.stdout.flush()
  tcflush(sys.stdin, TCIOFLUSH)
  exitCode = subprocess.call([user_dir + '/bin/cadmium', 'check'])
  tries = tries - 1

if exitCode == 1:
  print "Authentication with github has failed. Please check you username and password to make sure they are correct and try again."
  sys.exit(1)
