#
# Cookbook Name:: cadmium
# Recipe:: install-cli
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

load_recipe "maven::default"

user = "#{node[:cadmium][:cadmium_user]}"
home = Dir.home(user)

if !File.exists?("#{home}/#{node[:cadmium][:cli_install_path]}")
  directory "#{home}/#{node[:cadmium][:cli_install_path]}" do
  	owner "#{user}"
  	group "#{node[:cadmium][:cadmium_group]}"
  	mode 0755
  	action :create
  end
end

cookbook_file "cadmium" do
  path "/bin/cadmium"
  owner "root"
  group "root"
  mode 0755
  action :create_if_missing
end

if !File.exists?("#{home}/#{node[:cadmium][:cli_install_path]}/cadmium-cli.jar")
  maven "cadmium-cli" do
  	group_id "com.meltmedia.cadmium"
  	version "${project.version}"
  	dest "#{home}/#{node[:cadmium][:cli_install_path]}"
    owner "#{user}"
  	action :put
  end
end

remote_file "#{home}/#{node[:cadmium][:cli_install_path]}/github.token" do
  source "#{node[:cadmium][:cli_token_url]}"
  owner "#{user}"
  group "#{node[:cadmium][:cadmium_group]}"
  mode 0644
  
  action :create_if_missing
end
