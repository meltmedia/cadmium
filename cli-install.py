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

cadmium_version = '0.0.1-SNAPSHOT'

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

cadmium_commit = """#! /usr/bin/env python
import os, os.path, sys, subprocess, shutil

if not os.path.exists(os.path.expanduser('~/.cadmium/bin/cadmium')):
  print "Please run `cli-install.py` before running `cake deploy`"
  sys.exit(1)

if len(sys.argv) != 3:
  print "Please specify a \\"commit message\\" and a site url to deploy to.\\nUSAGE cadmium-deploy <message> <url>"
  sys.exit(1)

message = sys.argv[1]
url = sys.argv[2]

try:
  source_repo_url = subprocess.check_output(['git', 'config', '--get', 'remote.origin.url']).strip()
  source_sha = subprocess.check_output(['git', 'rev-parse', 'HEAD']).strip()
  source_branch = subprocess.check_output(['git', 'symbolic-ref', 'HEAD']).strip().split('/')
  if len(source_branch) > 1:
    source_branch = source_branch[len(source_branch) - 1]
  
  source = "{\\"repo\\":\\""+source_repo_url+"\\",\\"sha\\":\\""+source_sha+"\\",\\"branch\\":\\""+source_branch+"\\"}"

  if os.path.exists('out'):
    shutil.move('out', 'out-old')

  os.putenv('NODE_ENV', 'production')

  subprocess.call(['./node_modules/.bin/docpad', 'generate'])

  if not os.path.exists('out/META-INF'):
    os.mkdir('out/META-INF')

  if os.path.exists('out/META-INF/source'):
    os.remove('out/META-INF/source')

  fd = open('out/META-INF/source', 'w')
  try:
    fd.write(source)
    fd.flush()
  finally:
    fd.close()

  subprocess.call(['cadmium', 'validate', 'out'])

  subprocess.call(['cadmium', 'commit', '--quiet-auth', '-m', '"'+message+'"', 'out', url])

except subprocess.CalledProcessError:
  print "Please run this command from within a git repository."
finally:
  if os.path.exists('out-old'):
    if os.path.exists('out'):
      shutil.rmtree('out')
    shutil.move('out-old', 'out')

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

has_path = False
for item in path:
  if item == (user_dir + '/bin'):
    has_path = True
    break

if not has_path:
  print "Please run `. ~/.profile` before continuing!"
  has_path = (subprocess.call(['grep', '--silent', 'cadmium', os.path.expanduser('~/.profile')]) == 0)
  if not has_path:
    fd = open(os.path.expanduser('~/.profile'), 'a')
    try:
      fd.write('\nexport PATH="$PATH:'+user_dir+'/bin"\n')
      fd.flush()
      os.putenv('PATH', path_var + os.pathsep + user_dir + '/bin')
    finally:
      fd.close()

metaData = urllib.urlopen('http://nexus.meltdev.com/service/local/repo_groups/public/content/com/meltmedia/cadmium/cadmium-cli/maven-metadata.xml')
tree = xml.etree.ElementTree.fromstring( metaData.read() )
versioningNode = tree.find('versioning')
if versioningNode != None:
  releaseNode = versioningNode.find('release')
  if releaseNode != None:
    cadmium_version = releaseNode.text
  else:
    latestNode = versioningNode.find('latest')
    if latestNode != None:
      cadmium_version = latestNode.text

if os.path.exists(os.path.expanduser('~/.m2/repository/com/meltmedia/cadmium/cadmium-cli')):
  shutil.rmtree(os.path.expanduser('~/.m2/repository/com/meltmedia/cadmium/cadmium-cli'), True)

print 'Downloading latest version of cadmium...'
subprocess.call(['mvn', '-q', '-U', 'org.apache.maven.plugins:maven-dependency-plugin:2.4:get', '-Dartifact=com.meltmedia.cadmium:cadmium-cli:' + cadmium_version + ':jar', '-Ddest=' + os.path.expanduser('~/.cadmium/cadmium-cli.jar'), '-Dtransitive=false'])

sys.stdout.flush()
tcflush(sys.stdin, TCIOFLUSH)

subprocess.call([user_dir + '/bin/cadmium', 'check'])
