#
# Cookbook Name:: cadmium
# Attributes:: cadmium
#
# Copyright 2014, Meltmedia
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

default[:cadmium][:cli_token_url] = 'https://cloud-init.domain.com/secure/github.token'
default[:cadmium][:shared_content_root] = '/opt/cadmium'
default[:cadmium][:cli_install_path] = '/opt/cadmium/lib'
default[:cadmium][:environment] = 'development'
default[:cadmium][:ssh_dir] = '.ssh'
default[:cadmium][:ssh_key_priv] = nil
default[:cadmium][:github_teams] = nil
default[:cadmium][:cadmium_user] = 'ubuntu'
default[:cadmium][:cadmium_group] = 'ubuntu'
default[:cadmium][:system_user] = 'cadmium'
default[:cadmium][:system_group] = 'cadmium'

default[:cadmium][:war][:groupId] = 'com.meltmedia.cadmium'
default[:cadmium][:war][:artifactId] = 'cadmium-war'
default[:cadmium][:war][:version] = '${project.version}'
default[:cadmium][:domain] = 'localhost'
default[:cadmium][:port] = '8080'
default[:cadmium][:content][:repo] = 'https://github.com/meltmedia/cadmium.git'
default[:cadmium][:content][:branch] = 'manual'
default[:cadmium][:config][:repo] = 'https://github.com/meltmedia/cadmium.git'
default[:cadmium][:config][:branch] = 'config'

default[:cadmium][:jetty_root] = '/opt/cadmium/jetty'
default[:cadmium][:deploy_directory] = nil

default[:cadmium][:external_http_port] = '80'
default[:cadmium][:external_https_port] = '443'
