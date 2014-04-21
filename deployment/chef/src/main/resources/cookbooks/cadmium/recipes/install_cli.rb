#
# Cookbook Name:: cadmium
# Recipe:: install_cli
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

include_recipe "maven::default"

directory "#{node[:cadmium][:cli_install_path]}" do
  owner "#{node[:cadmium][:system_user]}"
  group "#{node[:cadmium][:system_group]}"
  mode 0755
  action :create
  notifies :put, "maven[cadmium-cli]", :delayed
end

template "/bin/cadmium" do
  source "cadmium.erb"
  mode 0755
  owner "root"
  group "root"
  action :create_if_missing
  notifies :put, "maven[cadmium-cli]", :delayed
end

maven "cadmium-cli" do
  group_id "com.meltmedia.cadmium"
  version "0.10.0"
  dest "#{node[:cadmium][:cli_install_path]}"
  owner "#{node[:cadmium][:system_user]}"
  action :nothing
end